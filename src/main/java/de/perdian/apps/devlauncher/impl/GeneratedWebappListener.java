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

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a valid web application within a temporary directory from a set
 * of source directories. This works a little bit like a Maven overlay, by
 * copying all the resources required for the web application into the target
 * directory before initializing the context.
 *
 * Any changes to files within the source directories will be recognized and
 * the updated files will be copied into the target web application directory.
 *
 * @author Christian Robert
 */

public class GeneratedWebappListener extends WebappListener {

    private static final Logger log = LoggerFactory.getLogger(GeneratedWebappListener.class);

    private File targetDirectory = null;
    private List<GeneratedWebappCopyDefinition> copyDefinitions = null;

    @Override
    protected File resolveWebapppDirectory() {
        File targetDirectory = this.getTargetDirectory();
        if (!targetDirectory.exists()) {
            log.debug("Creating web application target directory at: {}", targetDirectory.getAbsolutePath());
            targetDirectory.mkdirs();
        }
        return targetDirectory;
    }

    @Override
    protected Context createWebappContext(Tomcat tomcat, File webappDirectory) {

        // Make sure the target content has been added to the target directory
        this.initializeCopyDefinitions(tomcat);

        // Continue with the registration
        return super.createWebappContext(tomcat, webappDirectory);

    }

    /**
     * Make sure the source directories and the target directories are in sync
     */
    protected void initializeCopyDefinitions(Tomcat tomcat) {
        try {
            for (GeneratedWebappCopyDefinition copyDefinition : this.getCopyDefinitions()) {
                this.initializeCopyDefinition(copyDefinition, tomcat);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot copy resources into target directory: " + this.getTargetDirectory().getAbsolutePath(), e);
        }
    }

    /**
     * Make sure the source directories and the target directories are in sync
     */
    protected void initializeCopyDefinition(GeneratedWebappCopyDefinition copyDefinition, Tomcat tomcat) throws IOException {

        // First make sure all the files are copies
        File sourceDirectory = copyDefinition.getSourceDirectory();
        File targetDirectory = copyDefinition.getTargetDirectoryName() == null ? this.getTargetDirectory() : new File(this.getTargetDirectory(), copyDefinition.getTargetDirectoryName());
        this.copyResources(sourceDirectory, targetDirectory, copyDefinition);

        // Now add a change listener so that whenever a file will change in the
        // future we'll get notified and can react accordingly
        Path sourcePath = sourceDirectory.toPath();
        Path targetPath = targetDirectory.toPath();
        WatchService watchService = sourcePath.getFileSystem().newWatchService();
        sourcePath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        Thread watchServiceProcessorThread = new Thread(() -> {
            try {
                while (true) {
                    WatchKey nextWatchKey = watchService.take();
                    for (WatchEvent<?> watchEvent : nextWatchKey.pollEvents()) {
                        this.processWatchEvent(watchEvent, sourcePath, targetPath, copyDefinition);
                    }
                }
            } catch (InterruptedException e) {
                log.warn("WatchService has been stopped");
            } catch (ClosedWatchServiceException e) {
                log.trace("WatchService has been stopped");
            }
        });
        watchServiceProcessorThread.setName(this.getClass().getSimpleName() + "[ResourceWatcherThread for " + copyDefinition + "]");
        watchServiceProcessorThread.start();

        // Make sure the synchronization stops once the tomcat is stopped as
        // well
        tomcat.getServer().addLifecycleListener(event -> {
            if (Lifecycle.STOP_EVENT.equals(event.getType())) {
                try {
                    watchService.close();
                } catch(Exception e) {
                    log.warn("Error occured while closing WatchService", e);
                }
            }
        });

    }

    /**
     * Processes the watch event and execute the copy operation
     */
    protected void processWatchEvent(WatchEvent<?> watchEvent, Path sourcePath, Path targetPath, GeneratedWebappCopyDefinition copyDefinition) {
        System.err.println(watchEvent);
    }

    /**
     * Copies the resources from the source directory into the target directory
     */
    protected void copyResources(File sourceDirectory, File targetDirectory, GeneratedWebappCopyDefinition copyDefinition) throws IOException {
        File[] sourceFiles = copyDefinition.getFileFilter() == null ? sourceDirectory.listFiles() : sourceDirectory.listFiles(copyDefinition.getFileFilter());
        if (sourceFiles != null) {
            for (File sourceFile : sourceFiles) {
                File targetFile = new File(targetDirectory, sourceFile.getName());
                if (sourceFile.isDirectory()) {
                    this.copyResources(sourceFile, targetFile, copyDefinition);
                } else if (sourceFile.isFile() && sourceFile.canRead()) {
                    this.copyResource(sourceFile, targetFile);
                }
            }
        }
    }

    /**
     * Copy the given resource into the target directory
     *
     * @param sourceResource
     *     the resource to be copied
     * @param targetResource
     *     the target resource into which to copy the data
     * @throws IOException
     *     thrown if the copy operation fails
     */
    protected void copyResource(File sourceResource, File targetResource) throws IOException {

        boolean targetRequiresUpdate = !targetResource.exists();
        targetRequiresUpdate |= sourceResource.length() != targetResource.length();
        targetRequiresUpdate |= sourceResource.lastModified() > targetResource.lastModified();

        if (targetRequiresUpdate) {
            if (!targetResource.getParentFile().exists()) {
                targetResource.getParentFile().mkdirs();
            }
            Files.copy(sourceResource.toPath(), targetResource.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        }

    }
    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public List<GeneratedWebappCopyDefinition> getCopyDefinitions() {
        return this.copyDefinitions;
    }
    public void setCopyDefinitions(List<GeneratedWebappCopyDefinition> copyDefinitions) {
        this.copyDefinitions = copyDefinitions;
    }

    public File getTargetDirectory() {
        return this.targetDirectory;
    }
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}