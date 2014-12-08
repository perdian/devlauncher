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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherHelper;
import de.perdian.apps.devlauncher.DevLauncherListener;
import de.perdian.apps.devlauncher.support.CopyResourcesOperation;

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

    static final Logger log = LoggerFactory.getLogger(CopyResourcesListener.class);

    private FileFilter myFileFilter = null;
    private String myPrefix = "devlauncher.copy.";
    private boolean stateCopyRecursive = true;
    private boolean stateCopyUpdatedFilesOnly = true;
    private File mySourceDirectory = null;
    private File myTargetDirectory = null;
    private long myPollingInterval = -1;

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) {
        try {

            File sourceDirectory = this.resolveSourceDirectory(launcher);
            File targetDirectory = this.resolveTargetDirectory(launcher);

            CopyResourcesOperation copyResourcesOperation = new CopyResourcesOperation();
            copyResourcesOperation.setCopyRecursive(this.isCopyRecursive());
            copyResourcesOperation.setCopyUpdatedFilesOnly(this.isCopyUpdatedFilesOnly());
            copyResourcesOperation.setFileFilter(this.getFileFilter());
            copyResourcesOperation.setSourceDirectories(Arrays.asList(sourceDirectory));
            copyResourcesOperation.setTargetDirectory(targetDirectory);

            log.info("Copying resources from '{}' to '{}'", sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath());
            int copiedFileCount = copyResourcesOperation.copyFiles();
            log.info("Copied {} resources from source directory '{}' into target directory '{}'", copiedFileCount, sourceDirectory.getAbsolutePath(), targetDirectory.getAbsolutePath());

            // If we have an interval set, we also schedule a regular operation
            // that
            // performs the copy process
            if (this.getPollingInterval() > 0) {
                final Timer pollingTimer = copyResourcesOperation.createTimer(this.getPollingInterval());
                tomcat.getServer().addLifecycleListener(new LifecycleListener() {
                    @Override
                    public void lifecycleEvent(LifecycleEvent event) {
                        if (Lifecycle.STOP_EVENT.equals(event.getType())) {
                            pollingTimer.cancel();
                        }
                    }
                });
            }

        } catch (IOException e) {
            throw new RuntimeException("Cannot copy resources", e);
        }
    }

    /**
     * Resolves the source directory from which the files will be read
     */
    protected File resolveSourceDirectory(DevLauncher launcher) throws IOException {
        File sourceDirectory = this.getSourceDirectory();
        if (sourceDirectory == null) {
            String sourceDirectoryValue = System.getProperty(this.getPrefix() + ".sourceDirectory", null);
            sourceDirectory = sourceDirectoryValue == null || sourceDirectoryValue.length() <= 0 ? this.resolveDefaultSourceDirectory(launcher) : new File(sourceDirectoryValue).getCanonicalFile();
        }
        if (!sourceDirectory.exists()) {
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
     * Resolves the target directory into which the files will be copied
     */
    protected File resolveTargetDirectory(DevLauncher launcher) throws IOException {
        File targetDirectory = this.getTargetDirectory();
        if (targetDirectory == null) {
            String targetDirectoryKey = this.getPrefix() + "targetDirectory";
            String targetDirectoryValue = System.getProperty(targetDirectoryKey, null);
            if (targetDirectoryValue == null || targetDirectoryValue.length() <= 0) {
                throw new IllegalArgumentException("No configuration value has been set that determines the resource copy target directory (missing entry for key: '" + targetDirectoryKey + "' or property on listener)");
            } else {
                targetDirectory = new File(targetDirectoryValue).getCanonicalFile();
            }
        }
        if (!targetDirectory.exists()) {
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

    public CopyResourcesListener pollingInteral(long interval) {
        this.setPollingInterval(interval);
        return this;
    }
    public long getPollingInterval() {
        return this.myPollingInterval;
    }
    private void setPollingInterval(long pollingInterval) {
        this.myPollingInterval = pollingInterval;
    }

}