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

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Base class for all listeners providing a web application into the Tomcat
 *
 * @author Christian Robert
 */

public abstract class WebappListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(WebappListener.class);

    private String contextName = null;
    private File contextConfigurationFile = null;

    @Override
    public void customizeServer(Tomcat tomcat) {

        File webappDirectory = this.resolveWebapppDirectory();
        log.debug("Resolved webapp directory for webapp context '" + this.getContextName() + "' to: " + webappDirectory.getAbsolutePath());

        Context webappContext = this.createWebappContext(tomcat, webappDirectory);
        File contextConfigurationFile = this.getContextConfigurationFile();
        if (contextConfigurationFile != null) {
            if (!contextConfigurationFile.exists()) {
                log.warn("Resolved context configuration file for webapp context '" + this.getContextName() + "' not existing at: " + contextConfigurationFile.getAbsolutePath());
            } else {
                try {
                    log.debug("Resolved context configuration file for webapp context '" + this.getContextName() + "' to: " + contextConfigurationFile.getAbsolutePath());
                    webappContext.setConfigFile(contextConfigurationFile.toURI().toURL());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Invalid context configuration file: " + contextConfigurationFile.getAbsolutePath(), e);
                }
            }
        }

    }

    /**
     * Creates the web application context
     */
    protected Context createWebappContext(Tomcat tomcat, File webappDirectory) {
        try {
            return tomcat.addWebapp("/" + this.getContextName(), webappDirectory.getCanonicalPath());
        } catch (Exception e) {
            throw new RuntimeException("Cannot create webapp context for name " + this.getContextName() + " and directory " + webappDirectory.getAbsolutePath(), e);
        }
    }

    /**
     * Resolves the target directory that should be used as base for the web
     * application to be initialized
     */
    protected abstract File resolveWebapppDirectory();

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

}