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
package de.perdian.apps.devlauncher.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherHelper;
import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Sometimes a web application depends on external resources to be copied during
 * a deploy process. Since the DevLauncher doesn't really perform a deploy
 * process (in which a WAR file get's created) but launches the webserver
 * directly, we need to simulate such a deployment step.
 *
 * And one of these steps is copy resources.
 *
 * @author Christian Robert
 */

public class CopyResourcesListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(CopyResourcesListener.class);

    private FileFilter myFileFilter = null;
    private String myPrefix = "devlauncher.copy.";
    private boolean stateCopyRecursive = true;
    private boolean stateCopyUpdatedFilesOnly = true;
    private File mySourceDirectory = null;
    private File myTargetDirectory = null;

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) throws Exception {

        File sourceDirectory = this.resolveSourceDirectory(launcher);
        File targetDirectory = this.resolveTargetDirectory(launcher);

        int copiedFileCount = this.copyFiles(sourceDirectory, targetDirectory, this.isCopyRecursive());
        if(log.isInfoEnabled()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Copied ").append(copiedFileCount).append(" files from source directory '");
            logMessage.append(sourceDirectory.getAbsolutePath()).append("' into target directory '");
            logMessage.append(targetDirectory.getAbsolutePath()).append("'");
            log.info(logMessage.toString());
        }

    }

    private int copyFiles(File sourceDirectory, File targetDirectory, final boolean recursive) throws IOException {
        int copiedFileCount = 0;
        File[] sourceFiles = sourceDirectory.listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                if(file.isDirectory()) {
                    return recursive;
                } else {
                    return CopyResourcesListener.this.getFileFilter() == null ? true : CopyResourcesListener.this.getFileFilter().accept(file);
                }
            }
        });
        if(sourceFiles != null && sourceFiles.length > 0) {
            if(log.isDebugEnabled()) {
                StringBuilder logMessage = new StringBuilder();
                logMessage.append("Checking ").append(sourceFiles.length).append(" resource files from '");
                logMessage.append(sourceDirectory.getAbsolutePath()).append("' to copy into '");
                logMessage.append(targetDirectory.getAbsolutePath()).append("'");
                log.debug(logMessage.toString());
            }
            for(File sourceFile : sourceFiles) {
                if(sourceFile.isDirectory()) {
                    File targetSubDirectory = new File(targetDirectory, sourceFile.getName());
                    copiedFileCount += this.copyFiles(sourceFile, targetSubDirectory, true);
                } else {
                    File targetFile = new File(targetDirectory, sourceFile.getName());
                    if(this.checkFileNeedsUpdate(sourceFile, targetFile)) {
                        if(!targetFile.getParentFile().exists()) {
                            targetFile.getParentFile().mkdirs();
                        }
                        if(this.copyFile(sourceFile, targetFile)) {
                            copiedFileCount++;
                        }
                    }
                }
            }
        } else {
            log.debug("No files found to copy in directory: " + sourceDirectory.getAbsolutePath());
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
                    for(int data = sourceStream.read(); data > -1; data = sourceStream.read()) {
                        targetStream.write(data);
                    }
                } finally {
                    targetStream.close();
                }
            } finally {
                sourceStream.close();
            }
            return true;
        } catch(Exception e) {
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
            if(sourceFile.length() == targetFile.length()) {
                if(targetFile.lastModified() <= sourceFile.lastModified()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Resolves the source directory from which the files will be read
     */
    protected File resolveSourceDirectory(DevLauncher launcher) throws IOException {
        File sourceDirectory = this.getSourceDirectory();
        if(sourceDirectory == null) {
            String sourceDirectoryValue = System.getProperty(this.getPrefix() + ".sourceDirectory", null);
            sourceDirectory = sourceDirectoryValue == null || sourceDirectoryValue.length() <= 0 ? this.resolveDefaultSourceDirectory(launcher) : new File(sourceDirectoryValue).getCanonicalFile();
        }
        if(!sourceDirectory.exists()) {
            log.warn("Cannot find specified resource copy source directory at: " + sourceDirectory.getAbsolutePath());
        } else {
            log.debug("Resolved resource copy source directory to: " + sourceDirectory.getAbsolutePath());
        }
        return sourceDirectory;
    }

    /**
     * Resolves the source directory to be used if no specific directory has
     * been set using a system property
     */
    protected File resolveDefaultSourceDirectory(DevLauncher launcher) throws IOException {
        File projectDirectory = DevLauncherHelper.resolveProjectDirectory();
        return new File(projectDirectory, "src/main/resources/");
    }

    /**
     * Resolves the target directory from which the files will be copied
     */
    protected File resolveTargetDirectory(DevLauncher launcher) throws IOException {
        File targetDirectory = this.getTargetDirectory();
        if(targetDirectory == null) {
            String targetDirectoryKey = this.getPrefix() + "targetDirectory";
            String targetDirectoryValue = System.getProperty(targetDirectoryKey, null);
            if(targetDirectoryValue == null || targetDirectoryValue.length() <= 0) {
                throw new IllegalArgumentException("No configuration value has been set that determines the resource copy target directory (missing entry for key: '" + targetDirectoryKey + "' or property on listener)");
            } else {
                targetDirectory = new File(targetDirectoryValue).getCanonicalFile();
            }
        }
        if(!targetDirectory.exists()) {
            log.debug("Creating resource copy target directory at: " + targetDirectory.getAbsolutePath());
            targetDirectory.mkdirs();
        }
        return targetDirectory;
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public CopyResourcesListener fileFilter(FileFilter fileFilter) {
        this.setFileFilter(fileFilter);
        return this;
    }
    public FileFilter getFileFilter() {
        return this.myFileFilter;
    }
    private void setFileFilter(FileFilter fileFilter) {
        this.myFileFilter = fileFilter;
    }

    public CopyResourcesListener prefix(String prefix) {
        this.setPrefix(prefix);
        return this;
    }
    public String getPrefix() {
        return this.myPrefix;
    }
    private void setPrefix(String prefix) {
        this.myPrefix = prefix;
    }

    public CopyResourcesListener copyRecursive(boolean recursive) {
        this.setCopyRecursive(recursive);
        return this;
    }
    public boolean isCopyRecursive() {
        return this.stateCopyRecursive;
    }
    private void setCopyRecursive(boolean copyRecursive) {
        this.stateCopyRecursive = copyRecursive;
    }

    public CopyResourcesListener copyUpdatedFilesOnly(boolean updatedFilesOnly) {
        this.setCopyUpdatedFilesOnly(updatedFilesOnly);
        return this;
    }
    public boolean isCopyUpdatedFilesOnly() {
        return this.stateCopyUpdatedFilesOnly;
    }
    private void setCopyUpdatedFilesOnly(boolean copyUpdatedFilesOnly) {
        this.stateCopyUpdatedFilesOnly = copyUpdatedFilesOnly;
    }

    public CopyResourcesListener sourceDirectory(File directory) {
        this.setSourceDirectory(directory);
        return this;
    }
    public File getSourceDirectory() {
        return this.mySourceDirectory;
    }
    private void setSourceDirectory(File sourceDirectory) {
        this.mySourceDirectory = sourceDirectory;
    }

    public CopyResourcesListener targetDirectory(File directory) {
        this.setTargetDirectory(directory);
        return this;
    }
    public File getTargetDirectory() {
        return this.myTargetDirectory;
    }
    private void setTargetDirectory(File targetDirectory) {
        this.myTargetDirectory = targetDirectory;
    }

}