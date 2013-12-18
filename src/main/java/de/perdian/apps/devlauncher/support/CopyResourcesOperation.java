/*
 * DevLauncher
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.apps.devlauncher.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for an operation that copyies content from a set of source directories
 * into a target directory
 *
 * @author Christian Robert
 */

public class CopyResourcesOperation {

    static final Logger log = LoggerFactory.getLogger(CopyResourcesOperation.class);

    private List<File> mySourceDirectories = null;
    private File myTargetDirectory = null;
    private boolean stateCopyRecursive = true;
    private boolean stateCopyUpdatedFilesOnly = true;
    private FileFilter myFileFilter = null;

    // -------------------------------------------------------------------------
    // --- Execution -----------------------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Copy the files from the source(s) into the target directory
     *
     * @return
     *     the number of files that were copied
     * @throws IOException
     *     thrown if the copy operation fails
     */
    public int copyFiles() throws IOException {
        int copiedFiles = 0;
        for(File sourceDirectory : this.getSourceDirectories()) {
            copiedFiles += this.copyFiles(sourceDirectory, this.getTargetDirectory());
        }
        return copiedFiles;
    }

    /**
     * Copy the files from the source to the target directory
     *
     * @param sourceDirectory
     *     the source from which to copy the files
     * @param targetDirectory
     *     the target into which the files will be written
     * @return
     *     the number of files that were copied
     * @throws IOException
     *     thrown if the copy operation fails
     */
    protected int copyFiles(File sourceDirectory, File targetDirectory) throws IOException {
        int copiedFileCount = 0;
        File[] sourceFiles = sourceDirectory.listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                if (file.isDirectory()) {
                    return CopyResourcesOperation.this.isCopyRecursive();
                } else {
                    return CopyResourcesOperation.this.getFileFilter() == null ? true : CopyResourcesOperation.this.getFileFilter().accept(file);
                }
            }
        });
        if (sourceFiles != null && sourceFiles.length > 0) {
            log.trace("Checking {} resource files from '{}' to copy into '{}'", sourceFiles.length, sourceDirectory.getAbsoluteFile(), targetDirectory.getAbsoluteFile());
            for (File sourceFile : sourceFiles) {
                if (sourceFile.isDirectory()) {
                    File targetSubDirectory = new File(targetDirectory, sourceFile.getName());
                    copiedFileCount += this.copyFiles(sourceFile, targetSubDirectory);
                } else {
                    File targetFile = new File(targetDirectory, sourceFile.getName());
                    if (this.checkFileNeedsUpdate(sourceFile, targetFile)) {
                        if (!targetFile.getParentFile().exists()) {
                            targetFile.getParentFile().mkdirs();
                        }
                        if (this.copyFile(sourceFile, targetFile)) {
                            copiedFileCount++;
                        }
                    }
                }
            }
        } else {
            log.trace("No files found to copy in directory: {}", sourceDirectory.getAbsolutePath());
        }
        return copiedFileCount;
    }

    /**
     * Copy the file from it's source to the requested target location
     *
     * @param sourceFile
     *     the source file to copy
     * @param targetFile
     *     the target into which the data should be written
     * @return
     *     {@code true} if the file was written, {@code false} if the
     *     implementation decided not to copy the file
     */
    protected boolean copyFile(File sourceFile, File targetFile) throws IOException {
        try {
            InputStream sourceStream = new BufferedInputStream(new FileInputStream(sourceFile));
            try {
                OutputStream targetStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                try {
                    for (int data = sourceStream.read(); data > -1; data = sourceStream.read()) {
                        targetStream.write(data);
                    }
                    targetStream.flush();
                } finally {
                    targetStream.close();
                }
            } finally {
                sourceStream.close();
            }
            targetFile.setLastModified(sourceFile.lastModified());
            return true;
        } catch (Exception e) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Cannot copy resource file from '").append(sourceFile.getAbsolutePath());
            logMessage.append("' into target file '").append(targetFile.getAbsolutePath());
            logMessage.append("'");
            throw new IOException(logMessage.toString(), e);
        }
    }

    /**
     * Checks if the file needs to be updated
     */
    protected boolean checkFileNeedsUpdate(File sourceFile, File targetFile) {
        if(this.isCopyUpdatedFilesOnly()) {
            long sourceUpdate = sourceFile.lastModified();
            long targetUpdate = targetFile.lastModified();
            if(sourceUpdate <= targetUpdate) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    /**
     * Creates a new {@code Timer} on which a {@code TimerTask} has already been
     * scheduled at the given interval
     */
    public Timer createTimer(long interval) {
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override public void run() {
                log.trace("Executing polling for changed files");
                try {
                    int copiedFiles = CopyResourcesOperation.this.copyFiles();
                    if(copiedFiles <= 0) {
                        log.trace("No files copied during polling for changed files");
                    } else {
                        log.debug("{} files copied during polling for changed files", copiedFiles);
                    }
                } catch(IOException e) {
                    log.warn("Cannot copy resources files using '{}'", CopyResourcesOperation.this,  e);
                }
            }
        }, interval, interval);
        return timer;
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public List<File> getSourceDirectories() {
        return this.mySourceDirectories;
    }
    public void setSourceDirectories(List<File> sourceDirectories) {
        this.mySourceDirectories = sourceDirectories;
    }

    public File getTargetDirectory() {
        return this.myTargetDirectory;
    }
    public void setTargetDirectory(File targetDirectory) {
        this.myTargetDirectory = targetDirectory;
    }

    public boolean isCopyRecursive() {
        return this.stateCopyRecursive;
    }
    public void setCopyRecursive(boolean copyRecursive) {
        this.stateCopyRecursive = copyRecursive;
    }

    public boolean isCopyUpdatedFilesOnly() {
        return this.stateCopyUpdatedFilesOnly;
    }
    public void setCopyUpdatedFilesOnly(boolean copyUpdatedFilesOnly) {
        this.stateCopyUpdatedFilesOnly = copyUpdatedFilesOnly;
    }

    public FileFilter getFileFilter() {
        return this.myFileFilter;
    }
    public void setFileFilter(FileFilter fileFilter) {
        this.myFileFilter = fileFilter;
    }

}