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

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;

public class ExplodedWebappListener extends WebappListener {

    private static final Logger log = LoggerFactory.getLogger(ExplodedWebappListener.class);

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) {
        try {

            File webappDirectory = this.resolveWebappDirectory(launcher);
            log.debug("Resolved webapp directory for webapp context '" + this.getContextName() + "' to: " + webappDirectory.getAbsolutePath());

            Context webappContext = this.createWebappContext(tomcat, webappDirectory, launcher);
            File contextConfigurationFile = this.resolveContextConfigurationFile(launcher);
            if (contextConfigurationFile != null) {
                if (!contextConfigurationFile.exists()) {
                    log.warn("Resolved context configuration file for webapp context '" + this.getContextName() + "' not existing at: " + contextConfigurationFile.getAbsolutePath());
                } else {
                    log.debug("Resolved context configuration file for webapp context '" + this.getContextName() + "' to: " + contextConfigurationFile.getAbsolutePath());
                    webappContext.setConfigFile(contextConfigurationFile.toURI().toURL());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize web application context", e);
        }
    }

    protected Context createWebappContext(Tomcat tomcat, File webappDirectory, DevLauncher launcher) throws Exception {
        return tomcat.addWebapp("/" + this.getContextName(), webappDirectory.getCanonicalPath());
    }

    protected File resolveWebappDirectory(DevLauncher launcher) throws IOException {
        if (this.getWebappDirectory() != null) {
            if (!this.getWebappDirectory().exists()) {
                throw new FileNotFoundException("Specified webapp directory not existing at: " + this.getWebappDirectory().getAbsolutePath());
            } else {
                return this.getWebappDirectory();
            }
        } else {
            File projectDirectory = this.resolveProjectDirectory(launcher);
            String webappDirectoryName = this.getWebappDirectoryName();
            File webappDirectory = new File(projectDirectory, webappDirectoryName == null ? "src/main/webapp/" : webappDirectoryName);
            if (!webappDirectory.exists()) {
                throw new FileNotFoundException("Computed webapp directory not existing at: " + webappDirectory.getAbsolutePath());
            } else {
                return webappDirectory;
            }
        }
    }


    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public ExplodedWebappListener webappDirectory(File directory) {
        this.setWebappDirectory(directory);
        return this;
    }
    @Override
    public File getWebappDirectory() {
        return this.myWebappDirectory;
    }
    @Override
    private void setWebappDirectory(File webappDirectory) {
        this.myWebappDirectory = webappDirectory;
    }

    public ExplodedWebappListener webappDirectoryName(String directoryName) {
        this.setWebappDirectoryName(directoryName);
        return this;
    }
    public String getWebappDirectoryName() {
        return this.myWebappDirectoryName;
    }
    private void setWebappDirectoryName(String webappDirectoryName) {
        this.myWebappDirectoryName = webappDirectoryName;
    }

    public ExplodedWebappListener projectDirectory(File projecDirectory) {
        this.setProjectDirectory(projecDirectory);
        return this;
    }
    public File getProjectDirectory() {
        return this.myProjectDirectory;
    }
    private void setProjectDirectory(File projectDirectory) {
        this.myProjectDirectory = projectDirectory;
    }

    public ExplodedWebappListener projectDirectoryName(String directoryName) {
        this.setProjectDirectoryName(directoryName);
        return this;
    }
    public String getProjectDirectoryName() {
        return this.myProjectDirectoryName;
    }
    public void setProjectDirectoryName(String projectDirectoryName) {
        this.myProjectDirectoryName = projectDirectoryName;
    }

    public ExplodedWebappListener workspaceDirectory(File directory) {
        this.setWorkspaceDirectory(directory);
        return this;
    }
    public File getWorkspaceDirectory() {
        return this.myWorkspaceDirectory;
    }
    private void setWorkspaceDirectory(File workspaceDirectory) {
        this.myWorkspaceDirectory = workspaceDirectory;
    }

    public ExplodedWebappListener workspaceDirectoryName(String directoryName) {
        this.setWorkspaceDirectoryName(directoryName);
        return this;
    }
    public String getWorkspaceDirectoryName() {
        return this.myWorkspaceDirectoryName;
    }
    private void setWorkspaceDirectoryName(String workspaceDirectoryName) {
        this.myWorkspaceDirectoryName = workspaceDirectoryName;
    }

    public ExplodedWebappListener contextConfigurationFile(File file) {
        this.setContextConfigurationFile(file);
        return this;
    }

    public ExplodedWebappListener contextConfigurationFileName(String fileName) {
        this.setContextConfigurationFileName(fileName);
        return this;
    }
    public String getContextConfigurationFileName() {
        return this.myContextConfigurationFileName;
    }
    private void setContextConfigurationFileName(String contextConfigurationFileName) {
        this.myContextConfigurationFileName = contextConfigurationFileName;
    }

    public ExplodedWebappListener contextConfigurationFileResolver(ContextConfigurationFileResolver resolver) {
        this.setContextConfigurationFileResolver(resolver);
        return this;
    }
    public ContextConfigurationFileResolver getContextConfigurationFileResolver() {
        return this.myContextConfigurationFileResolver;
    }
    private void setContextConfigurationFileResolver(ContextConfigurationFileResolver contextConfigurationFileResolver) {
        this.myContextConfigurationFileResolver = contextConfigurationFileResolver;
    }

}