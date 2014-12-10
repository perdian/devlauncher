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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExplodedWebappListenerBuilder {

    private String webappDirectoryName = "src/main/webapp";
    private File projectDirectory = null;
    private String projectDirectoryName = null;
    private File workspaceDirectory = null;
    private String workspaceDirectoryName = null;
    private String contextName = null;
    private File contextConfigurationFile = null;
    private String contextConfigurationFileName = null;

    public ExplodedWebappListener createListener() throws IOException {
        ExplodedWebappListener listener = new ExplodedWebappListener();
        listener.setContextName(this.getContextName());
        listener.setContextConfigurationFile(this.resolveContextConfigurationFile());
        return listener;
    }

    protected File resolveContextConfigurationFile() throws IOException {
        if (this.getContextConfigurationFile() != null) {
            return this.getContextConfigurationFile();
        } else if (this.getContextConfigurationFileName() != null) {
            File projectDirectory = this.resolveProjectDirectory();
            return new File(projectDirectory, this.getContextConfigurationFileName()).getCanonicalFile();
        } else {
            return null;
        }
    }

    protected File resolveProjectDirectory() throws IOException {
        if (this.getProjectDirectory() != null) {
            if (!this.getProjectDirectory().exists()) {
                throw new FileNotFoundException("Specified project directory not existing at: " + this.getProjectDirectory().getAbsolutePath());
            } else {
                return this.getProjectDirectory();
            }
        } else {
            File workspaceDirectory = this.resolveWorkspaceDirectory();
            String projectDirectoryName = this.getProjectDirectoryName() == null ? this.getContextName() : this.getProjectDirectoryName();
            File projectDirectory = new File(workspaceDirectory, projectDirectoryName).getCanonicalFile();
            if (!projectDirectory.exists()) {
                throw new FileNotFoundException("Computed project directory not existing at: " + projectDirectory.getAbsolutePath());
            } else {
                return projectDirectory;
            }
        }
    }

    protected File resolveWorkspaceDirectory() throws IOException {
        if (this.getWorkspaceDirectory() != null) {
            if (!this.getWorkspaceDirectory().exists()) {
                throw new FileNotFoundException("Specified workspace directory not existing at: " + this.getWorkspaceDirectory().getAbsolutePath());
            } else {
                return this.getWorkspaceDirectory();
            }
        } else {
            String workspaceDirectoryValue = System.getProperty("devlauncher.workspaceDirectory", null);
            File workspaceComputedDirectory = workspaceDirectoryValue != null && workspaceDirectoryValue.length() > 0 ? new File(workspaceDirectoryValue).getCanonicalFile() : null;
            File workspaceDirectory = workspaceComputedDirectory == null ? new File(".").getCanonicalFile().getParentFile() : workspaceComputedDirectory;
            if (!workspaceDirectory.exists()) {
                throw new FileNotFoundException("Computed workspace directory not existing at: " + workspaceDirectory.getAbsolutePath());
            } else {
                return workspaceDirectory;
            }
        }
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public ExplodedWebappListenerBuilder webappDirectoryName(String directoryName) {
        this.setWebappDirectoryName(directoryName);
        return this;
    }
    private String getWebappDirectoryName() {
        return this.webappDirectoryName;
    }
    private void setWebappDirectoryName(String webappDirectoryName) {
        this.webappDirectoryName = webappDirectoryName;
    }

    public ExplodedWebappListenerBuilder projectDirectory(File projectDirectory) {
        this.setProjectDirectory(projectDirectory);
        return this;
    }
    private File getProjectDirectory() {
        return this.projectDirectory;
    }
    private void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public ExplodedWebappListenerBuilder projectDirectoryName(String projectDirectoryName) {
        this.setProjectDirectoryName(projectDirectoryName);
        return this;
    }
    private String getProjectDirectoryName() {
        return this.projectDirectoryName;
    }
    private void setProjectDirectoryName(String projectDirectoryName) {
        this.projectDirectoryName = projectDirectoryName;
    }

    public ExplodedWebappListenerBuilder workspaceDirectory(File workspaceDirectory) {
        this.setWorkspaceDirectory(workspaceDirectory);
        return this;
    }
    private File getWorkspaceDirectory() {
        return this.workspaceDirectory;
    }
    private void setWorkspaceDirectory(File workspaceDirectory) {
        this.workspaceDirectory = workspaceDirectory;
    }

    public ExplodedWebappListenerBuilder workspaceDirectoryName(String workspaceDirectoryName) {
        this.setWorkspaceDirectoryName(workspaceDirectoryName);
        return this;
    }
    private String getWorkspaceDirectoryName() {
        return this.workspaceDirectoryName;
    }
    private void setWorkspaceDirectoryName(String workspaceDirectoryName) {
        this.workspaceDirectoryName = workspaceDirectoryName;
    }

    public ExplodedWebappListenerBuilder contextName(String contextName) {
        this.setContextName(contextName);
        return this;
    }
    private String getContextName() {
        return this.contextName;
    }
    private void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public ExplodedWebappListenerBuilder contextConfigurationFile(File contextConfigurationFile) {
        this.setContextConfigurationFile(contextConfigurationFile);
        return this;
    }
    private File getContextConfigurationFile() {
        return this.contextConfigurationFile;
    }
    private void setContextConfigurationFile(File contextConfigurationFile) {
        this.contextConfigurationFile = contextConfigurationFile;
    }

    public ExplodedWebappListenerBuilder contextConfigurationFileName(String contextConfigurationFileName) {
        this.setContextConfigurationFileName(contextConfigurationFileName);
        return this;
    }
    private String getContextConfigurationFileName() {
        return this.contextConfigurationFileName;
    }
    private void setContextConfigurationFileName(String contextConfigurationFileName) {
        this.contextConfigurationFileName = contextConfigurationFileName;
    }

}