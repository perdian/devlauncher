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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExplodedWebappListener extends WebappListener {

    private Path webappDirectory = null;
    private String webappDirectoryName = "src/main/webapp";
    private Path projectDirectory = null;
    private String projectDirectoryName = null;
    private Path workspaceDirectory = null;

    public ExplodedWebappListener(String contextName) {
        super(contextName);
    }

    @Override
    protected Path resolveContextConfigurationFile() throws IOException {
        if (this.getContextConfigurationFile() != null) {
            return this.getContextConfigurationFile();
        } else if (this.getContextConfigurationFileName() != null) {
            return this.resolveProjectDirectory().resolve(this.getContextConfigurationFileName());
        } else {
            return null;
        }
    }

    @Override
    protected Path resolveWebappDirectory() throws IOException {
        if (this.getWebappDirectory() != null) {
            if (!Files.exists(this.getWebappDirectory())) {
                throw new FileNotFoundException("Specified webapp directory not existing at: " + this.getWebappDirectory());
            } else {
                return this.getWebappDirectory();
            }
        } else {
            Path projectDirectory = this.resolveProjectDirectory();
            String webappDirectoryName = this.getWebappDirectoryName();
            Path webappDirectory = projectDirectory.resolve(webappDirectoryName == null ? "src/main/webapp/" : webappDirectoryName);
            if (!Files.exists(webappDirectory)) {
                throw new FileNotFoundException("Computed webapp directory not existing at: " + webappDirectory);
            } else {
                return webappDirectory;
            }
        }
    }

    protected Path resolveProjectDirectory() throws IOException {
        if (this.getProjectDirectory() != null) {
            if (!Files.exists(this.getProjectDirectory())) {
                throw new FileNotFoundException("Specified project directory not existing at: " + this.getProjectDirectory());
            } else {
                return this.getProjectDirectory();
            }
        } else {
            Path workspaceDirectory = this.resolveWorkspaceDirectory();
            String projectDirectoryName = this.getProjectDirectoryName() == null ? this.getContextName() : this.getProjectDirectoryName();
            Path projectDirectory = workspaceDirectory.resolve(projectDirectoryName);
            if (!Files.exists(projectDirectory)) {
                throw new FileNotFoundException("Computed project directory not existing at: " + projectDirectory);
            } else {
                return projectDirectory;
            }
        }
    }

    protected Path resolveWorkspaceDirectory() throws IOException {
        if (this.getWorkspaceDirectory() != null) {
            if (!Files.exists(this.getWorkspaceDirectory())) {
                throw new FileNotFoundException("Specified workspace directory not existing at: " + this.getWorkspaceDirectory());
            } else {
                return this.getWorkspaceDirectory();
            }
        } else {
            Path workspaceDirectory = new File(".").getCanonicalFile().getParentFile().toPath();
            if (!Files.exists(workspaceDirectory)) {
                throw new FileNotFoundException("Computed workspace directory not existing at: " + workspaceDirectory);
            } else {
                return workspaceDirectory;
            }
        }
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public ExplodedWebappListener webappDirectory(Path webappDirectory) {
        this.setWebappDirectory(webappDirectory);
        return this;
    }
    public Path getWebappDirectory() {
        return this.webappDirectory;
    }
    private void setWebappDirectory(Path webappDirectory) {
        this.webappDirectory = webappDirectory;
    }

    public ExplodedWebappListener webappDirectoryName(String directoryName) {
        this.setWebappDirectoryName(directoryName);
        return this;
    }
    public String getWebappDirectoryName() {
        return this.webappDirectoryName;
    }
    private void setWebappDirectoryName(String webappDirectoryName) {
        this.webappDirectoryName = webappDirectoryName;
    }

    public ExplodedWebappListener projectDirectory(Path projectDirectory) {
        this.setProjectDirectory(projectDirectory);
        return this;
    }
    public Path getProjectDirectory() {
        return this.projectDirectory;
    }
    private void setProjectDirectory(Path projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public ExplodedWebappListener projectDirectoryName(String projectDirectoryName) {
        this.setProjectDirectoryName(projectDirectoryName);
        return this;
    }
    public String getProjectDirectoryName() {
        return this.projectDirectoryName;
    }
    private void setProjectDirectoryName(String projectDirectoryName) {
        this.projectDirectoryName = projectDirectoryName;
    }

    public ExplodedWebappListener workspaceDirectory(Path workspaceDirectory) {
        this.setWorkspaceDirectory(workspaceDirectory);
        return this;
    }
    public Path getWorkspaceDirectory() {
        return this.workspaceDirectory;
    }
    private void setWorkspaceDirectory(Path workspaceDirectory) {
        this.workspaceDirectory = workspaceDirectory;
    }

}