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

import org.apache.catalina.startup.Tomcat;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Base class for all listeners providing a web application into the Tomcat
 *
 * @author Christian Robert
 */

public abstract class WebappListener implements DevLauncherListener {

    private String contextName = null;
    private File contextConfigurationFile = null;
    private File webappDirectory = null;

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) {
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public String getContextName() {
        return this.contextName;
    }
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public File getContextConfigurationFile() {
        return this.contextConfigurationFile;
    }
    public void setContextConfigurationFile(File contextConfigurationFile) {
        this.contextConfigurationFile = contextConfigurationFile;
    }

    public File getWebappDirectory() {
        return this.webappDirectory;
    }
    public void setWebappDirectory(File webappDirectory) {
        this.webappDirectory = webappDirectory;
    }

}