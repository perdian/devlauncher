/*
 * DevLauncher
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.apps.devlauncher.impl;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GeneratedWebappCopyHandler implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(GeneratedWebappCopyHandler.class);

    private boolean closed = false;
    private Map<WatchKey, PathPair> keyToPairMap = null;
    private WatchService watchService = null;
    private Predicate<Path> fileFilter = null;

    public static GeneratedWebappCopyHandler create(Path sourcePath, Path targetPath, Predicate<Path> fileFilter) throws IOException {

        GeneratedWebappCopyHandler copyHandler = new GeneratedWebappCopyHandler();
        copyHandler.setFileFilter(fileFilter == null ? file -> true : fileFilter);
        copyHandler.setKeyToPairMap(new HashMap<>());
        copyHandler.setWatchService(sourcePath.getFileSystem().newWatchService());

        // First make sure the initial copy process is complete
        int copiedResources = copyHandler.copyResources(sourcePath, targetPath);
        if (copiedResources > 0) {
            log.debug("Copied {} resources from {} to {}", copiedResources, sourcePath, targetPath);
        }

        // Now register the listener for all following copy processes
        copyHandler.registerWatchServiceOnPath(sourcePath, targetPath);

        // Start the processor Thread that will do the actual work of
        // transfering the files from their source to the target
        Thread watchServiceProcessorThread = new Thread(copyHandler::handleEvents);
        watchServiceProcessorThread.setName(GeneratedWebappCopyHandler.class.getSimpleName() + "[ResourceWatcherThread for " + sourcePath + "->" + targetPath + "]");
        watchServiceProcessorThread.start();

        // We're done!
        return copyHandler;

    }

    @Override
    public synchronized void close() throws IOException {
        this.setClosed(true);
        this.getWatchService().close();
    }

    private void registerWatchServiceOnPath(Path sourcePath, Path targetPath) {
        try {

            PathPair pathPair = new PathPair(sourcePath, targetPath);
            WatchKey watchKey = sourcePath.register(this.getWatchService(), StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            this.getKeyToPairMap().put(watchKey, pathPair);

            Files.list(sourcePath)
                .filter(Files::isDirectory)
                .forEach(sourceChildPath -> {
                    Path targetChildPath = targetPath.resolve(sourceChildPath.getFileName());
                    this.registerWatchServiceOnPath(sourceChildPath, targetChildPath);
                });

        } catch (IOException e) {
            log.debug("Cannot process source directory: {}", sourcePath);
        }
    }

    private void handleEvents() {
        try {
            while (!this.isClosed()) {
                WatchKey nextWatchKey = this.getWatchService().take();
                try {
                    this.handleWatchKey(nextWatchKey);
                } catch (Exception e) {
                    log.debug("Cannot handle WatchKey: {}", nextWatchKey, e);
                } finally {
                    if (!nextWatchKey.reset()) {
                        log.debug("Removing watch key: " + nextWatchKey);
                        this.getKeyToPairMap().remove(nextWatchKey);
                    }
                }
            }
        } catch (ClosedWatchServiceException e) {
            log.trace("File watching service has been closed", e);
        } catch (InterruptedException e) {
            log.warn("File watching service has been interrupted", e);
        }
    }

    private void handleWatchKey(WatchKey nextWatchKey) {
        for (WatchEvent<?> watchEvent : nextWatchKey.pollEvents()) {

            PathPair pathPair = this.getKeyToPairMap().get(nextWatchKey);
            Path targetDirectoryPath = pathPair == null ? null : pathPair.getTargetPath();
            Path sourceFilePath = pathPair == null ? null : pathPair.getSourcePath().resolve((Path)watchEvent.context());
            Path targetFilePath = targetDirectoryPath == null || sourceFilePath == null ? null : targetDirectoryPath.resolve(sourceFilePath.getFileName());
            if (sourceFilePath != null && targetFilePath != null) {
                try {
                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(watchEvent.kind())) {
                        this.handlePathCreated(sourceFilePath, targetFilePath);
                    } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(watchEvent.kind())) {
                        this.handlePathModified(sourceFilePath, targetFilePath);
                    } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(watchEvent.kind())) {
                        this.handlePathDeleted(sourceFilePath, targetFilePath);
                    }
                } catch (IOException e) {
                    log.warn("Cannot perform operation for kind {} on target directory: {}", watchEvent.kind(), targetFilePath, e);
                }
            } else {
                this.getKeyToPairMap().remove(nextWatchKey);
            }

        }
    }

    private void handlePathDeleted(Path sourceFilePath, Path targetFilePath) throws IOException {
        if (Files.isDirectory(targetFilePath)) {
            GeneratedWebappCopyHandler.deleteRecursively(targetFilePath);
        } else if (Files.isRegularFile(targetFilePath)) {
            Files.deleteIfExists(targetFilePath);
        }
    }

    private void handlePathModified(Path sourceFilePath, Path targetFilePath) throws IOException {
        if (Files.isRegularFile(sourceFilePath)) {
            this.copyResource(sourceFilePath, targetFilePath);
        }
    }

    private void handlePathCreated(Path sourceFilePath, Path targetFilePath) throws IOException {
        if (Files.isDirectory(sourceFilePath)) {

            // Copy any content that may already be inside the directory
            this.copyResources(sourceFilePath, targetFilePath);

            // Register for upcoming changes
            this.registerWatchServiceOnPath(sourceFilePath, targetFilePath);

        }
    }

    // -------------------------------------------------------------------------
    // --- Copy implementations ------------------------------------------------
    // -------------------------------------------------------------------------

    private int copyResources(Path sourcePath, Path targetPath) throws IOException {
        List<Path> sourceChildren = Files.list(sourcePath).filter(this.getFileFilter()).collect(Collectors.toList());
        if (sourceChildren != null) {
            int copiedFiles = 0;
            for (Path sourceChild : sourceChildren) {
                Path targetChild = targetPath.resolve(sourceChild.getFileName());
                if (Files.isDirectory(sourceChild)) {
                    copiedFiles += this.copyResources(sourceChild, targetChild);
                } else if (Files.isReadable(sourceChild)) {
                    if (this.copyResource(sourceChild, targetChild)) {
                        copiedFiles++;
                    }
                }
            }
            return copiedFiles;
        } else {
            return 0;
        }
    }

    private boolean copyResource(Path sourcePath, Path targetPath) throws IOException {

        boolean targetRequiresUpdate = !Files.exists(targetPath);
        targetRequiresUpdate = targetRequiresUpdate || Files.size(sourcePath) != Files.size(targetPath);
        targetRequiresUpdate = targetRequiresUpdate || Files.getLastModifiedTime(sourcePath).toMillis() > Files.getLastModifiedTime(targetPath).toMillis();

        if (targetRequiresUpdate) {
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            Files.copy(sourcePath, targetPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } else {
            return false;
        }

    }

    // -------------------------------------------------------------------------
    // --- Helper methods ------------------------------------------------------
    // -------------------------------------------------------------------------

    static void deleteRecursively(Path path) {
        try {
            if(Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    Files.list(path).forEach(GeneratedWebappCopyHandler::deleteRecursively);
                }
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot delete path: " + path, e);
        }
    }


    // -------------------------------------------------------------------------
    // --- Inner classes -------------------------------------------------------
    // -------------------------------------------------------------------------

    static class PathPair {

        private Path sourcePath = null;
        private Path targetPath = null;

        PathPair(Path sourcePath, Path targetPath) {
            this.setSourcePath(sourcePath);
            this.setTargetPath(targetPath);
        }

        // ---------------------------------------------------------------------
        // --- Property access methods -----------------------------------------
        // ---------------------------------------------------------------------

        Path getSourcePath() {
            return this.sourcePath;
        }
        private void setSourcePath(Path sourcePath) {
            this.sourcePath = sourcePath;
        }

        Path getTargetPath() {
            return this.targetPath;
        }
        private void setTargetPath(Path targetPath) {
            this.targetPath = targetPath;
        }

    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    private WatchService getWatchService() {
        return this.watchService;
    }
    private void setWatchService(WatchService watchService) {
        this.watchService = watchService;
    }

    private Map<WatchKey, PathPair> getKeyToPairMap() {
        return this.keyToPairMap;
    }
    private void setKeyToPairMap(Map<WatchKey, PathPair> keyToPairMap) {
        this.keyToPairMap = keyToPairMap;
    }

    private boolean isClosed() {
        return this.closed;
    }
    private void setClosed(boolean closed) {
        this.closed = closed;
    }

    private Predicate<Path> getFileFilter() {
        return this.fileFilter;
    }
    private void setFileFilter(Predicate<Path> fileFilter) {
        this.fileFilter = fileFilter;
    }

}