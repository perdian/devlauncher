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

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherListener;

public class WebappListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(WebappListener.class);

    private String myContextName = null;
    private File myWebappDirectory = null;
    private String myWebappDirectoryName = "src/main/webapp";
    private File myProjectDirectory = null;
    private String myProjectDirectoryName = null;
    private File myWorkspaceDirectory = null;
    private String myWorkspaceDirectoryName = null;
    private File myContextConfigurationFile = null;
    private String myContextConfigurationFileName = null;
    private ContextConfigurationFileResolver myContextConfigurationFileResolver = null;

    /**
     * Creates a new listener instance
     *
     * @param contextName
     *     the name of the context under which the webapp will be made available
     *     in the embedded Tomcat instance
     */
    public WebappListener(String contextName) {
        this.setContextName(contextName);
    }

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) throws Exception {

        File webappDirectory = this.resolveWebappDirectory(launcher);
        log.debug("Resolved webapp directory for webapp context '" + this.getContextName() + "' to: " + webappDirectory.getAbsolutePath());

        Context webappContext = this.createWebappContext(tomcat, webappDirectory, launcher);
        File contextConfigurationFile = this.resolveContextConfigurationFile(launcher);
        if(contextConfigurationFile != null) {
            if(!contextConfigurationFile.exists()) {
                log.warn("Resolved context configuration file for webapp context '" + this.getContextName() + "' not existing at: " + contextConfigurationFile.getAbsolutePath());
            } else {
                log.debug("Resolved context configuration file for webapp context '" + this.getContextName() + "' to: " + contextConfigurationFile.getAbsolutePath());
                webappContext.setConfigFile(contextConfigurationFile.toURI().toURL());
            }
        }

    }

    protected Context createWebappContext(Tomcat tomcat, File webappDirectory, DevLauncher launcher) throws Exception {
        return tomcat.addWebapp("/" + this.getContextName(), webappDirectory.getCanonicalPath());
    }

    protected File resolveContextConfigurationFile(DevLauncher launcher) throws IOException {
        if(this.getContextConfigurationFileResolver() != null) {
            File projectDirectory = this.resolveProjectDirectory(launcher);
            return this.getContextConfigurationFileResolver().resolveContextConfigurationFile(this.getContextName(), projectDirectory, launcher);
        } else if(this.getContextConfigurationFile() != null) {
            return this.getContextConfigurationFile();
        } else if(this.getContextConfigurationFileName() != null) {
            File projectDirectory = this.resolveProjectDirectory(launcher);
            return new File(projectDirectory, this.getContextConfigurationFileName()).getCanonicalFile();
        } else {
            return null;
        }
    }

    protected File resolveWebappDirectory(DevLauncher launcher) throws IOException {
        if(this.getWebappDirectory() != null) {
            if(!this.getWebappDirectory().exists()) {
                throw new FileNotFoundException("Specified webapp directory not existing at: " + this.getWebappDirectory().getAbsolutePath());
            } else {
                return this.getWebappDirectory();
            }
        } else {
            File projectDirectory = this.resolveProjectDirectory(launcher);
            String webappDirectoryName = this.getWebappDirectoryName();
            File webappDirectory = new File(projectDirectory, webappDirectoryName == null ? "src/main/webapp/" : webappDirectoryName);
            if(!webappDirectory.exists()) {
                throw new FileNotFoundException("Computed webapp directory not existing at: " + webappDirectory.getAbsolutePath());
            } else {
                return webappDirectory;
            }
        }
    }

    private File resolveProjectDirectory(DevLauncher launcher) throws IOException {
        if(this.getProjectDirectory() != null) {
            if(!this.getProjectDirectory().exists()) {
                throw new FileNotFoundException("Specified project directory not existing at: " + this.getProjectDirectory().getAbsolutePath());
            } else {
                return this.getProjectDirectory();
            }
        } else {
            File workspaceDirectory = this.resolveWorkspaceDirectory(launcher);
            String projectDirectoryName = this.getProjectDirectoryName() == null ? this.getContextName() : this.getProjectDirectoryName();
            File projectDirectory = new File(workspaceDirectory, projectDirectoryName).getCanonicalFile();
            if(!projectDirectory.exists()) {
                throw new FileNotFoundException("Computed project directory not existing at: " + projectDirectory.getAbsolutePath());
            } else {
                return projectDirectory;
            }
        }
    }

    private File resolveWorkspaceDirectory(DevLauncher launcher) throws IOException {
        if(this.getWorkspaceDirectory() != null) {
            if(!this.getWorkspaceDirectory().exists()) {
                throw new FileNotFoundException("Specified workspace directory not existing at: " + this.getWorkspaceDirectory().getAbsolutePath());
            } else {
                return this.getWorkspaceDirectory();
            }
        } else {
            String workspaceDirectoryValue = System.getProperty("devlauncher.workspaceDirectory", null);
            File workspaceComputedDirectory = workspaceDirectoryValue != null && workspaceDirectoryValue.length() > 0 ? new File(workspaceDirectoryValue).getCanonicalFile() : null;
            File workspaceDirectory = workspaceComputedDirectory == null ? new File(".").getCanonicalFile().getParentFile() : workspaceComputedDirectory;
            if(!workspaceDirectory.exists()) {
                throw new FileNotFoundException("Computed workspace directory not existing at: " + workspaceDirectory.getAbsolutePath());
            } else {
                return workspaceDirectory;
            }
        }
    }

    // -------------------------------------------------------------------------
    // --- Inner classes -------------------------------------------------------
    // -------------------------------------------------------------------------

    public static interface ContextConfigurationFileResolver {

        /**
         * Resolves the configuration file according to the settings in the
         * launcher
         */
        public File resolveContextConfigurationFile(String contextName, File projectDirectory, DevLauncher launcher) throws IOException;

    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public String getContextName() {
        return this.myContextName;
    }
    private void setContextName(String contextName) {
        this.myContextName = contextName;
    }

    public WebappListener webappDirectory(File directory) {
        this.setWebappDirectory(directory);
        return this;
    }
    public File getWebappDirectory() {
        return this.myWebappDirectory;
    }
    private void setWebappDirectory(File webappDirectory) {
        this.myWebappDirectory = webappDirectory;
    }

    public WebappListener webappDirectoryName(String directoryName) {
        this.setWebappDirectoryName(directoryName);
        return this;
    }
    public String getWebappDirectoryName() {
        return this.myWebappDirectoryName;
    }
    private void setWebappDirectoryName(String webappDirectoryName) {
        this.myWebappDirectoryName = webappDirectoryName;
    }

    public WebappListener projectDirectory(File projecDirectory) {
        this.setProjectDirectory(projecDirectory);
        return this;
    }
    public File getProjectDirectory() {
        return this.myProjectDirectory;
    }
    private void setProjectDirectory(File projectDirectory) {
        this.myProjectDirectory = projectDirectory;
    }

    public WebappListener projectDirectoryName(String directoryName) {
        this.setProjectDirectoryName(directoryName);
        return this;
    }
    public String getProjectDirectoryName() {
        return this.myProjectDirectoryName;
    }
    public void setProjectDirectoryName(String projectDirectoryName) {
        this.myProjectDirectoryName = projectDirectoryName;
    }

    public WebappListener workspaceDirectory(File directory) {
        this.setWorkspaceDirectory(directory);
        return this;
    }
    public File getWorkspaceDirectory() {
        return this.myWorkspaceDirectory;
    }
    private void setWorkspaceDirectory(File workspaceDirectory) {
        this.myWorkspaceDirectory = workspaceDirectory;
    }

    public WebappListener workspaceDirectoryName(String directoryName) {
        this.setWorkspaceDirectoryName(directoryName);
        return this;
    }
    public String getWorkspaceDirectoryName() {
        return this.myWorkspaceDirectoryName;
    }
    private void setWorkspaceDirectoryName(String workspaceDirectoryName) {
        this.myWorkspaceDirectoryName = workspaceDirectoryName;
    }

    public WebappListener contextConfigurationFile(File file) {
        this.setContextConfigurationFile(file);
        return this;
    }
    public File getContextConfigurationFile() {
        return this.myContextConfigurationFile;
    }
    private void setContextConfigurationFile(File contextConfigurationFile) {
        this.myContextConfigurationFile = contextConfigurationFile;
    }

    public WebappListener contextConfigurationFileName(String fileName) {
        this.setContextConfigurationFileName(fileName);
        return this;
    }
    public String getContextConfigurationFileName() {
        return this.myContextConfigurationFileName;
    }
    private void setContextConfigurationFileName(String contextConfigurationFileName) {
        this.myContextConfigurationFileName = contextConfigurationFileName;
    }

    public WebappListener contextConfigurationFileResolver(ContextConfigurationFileResolver resolver) {
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