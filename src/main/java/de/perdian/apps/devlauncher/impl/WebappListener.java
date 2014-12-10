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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Base class for all listeners providing a web application into the Tomcat
 *
 * @author Christian Robert
 */

public abstract class WebappListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(WebappListener.class);

    private String contextName = null;
    private Path contextConfigurationFile = null;

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher devLauncher) {

        Path webappDirectory = this.resolveWebapppDirectory();
        log.debug("Resolved webapp directory for webapp context '" + this.getContextName() + "' to: " + webappDirectory);

        Context webappContext = this.createWebappContext(tomcat, webappDirectory);
        Path contextConfigurationFile = this.getContextConfigurationFile();
        if (contextConfigurationFile != null) {
            if (!Files.exists(contextConfigurationFile)) {
                log.warn("Resolved context configuration file for webapp context '" + this.getContextName() + "' not existing at: " + contextConfigurationFile);
            } else {
                try {
                    log.debug("Resolved context configuration file for webapp context '" + this.getContextName() + "' to: " + contextConfigurationFile);
                    webappContext.setConfigFile(contextConfigurationFile.toUri().toURL());
                } catch (IOException e) {
                    throw new IllegalArgumentException("Invalid context configuration file: " + contextConfigurationFile, e);
                }
            }
        }

    }

    /**
     * Creates the web application context
     */
    protected Context createWebappContext(Tomcat tomcat, Path webappDirectory) {
        try {
            return tomcat.addWebapp("/" + this.getContextName(), webappDirectory.toFile().getCanonicalPath());
        } catch (Exception e) {
            throw new RuntimeException("Cannot create webapp context for name " + this.getContextName() + " and directory " + webappDirectory, e);
        }
    }

    /**
     * Resolves the target directory that should be used as base for the web
     * application to be initialized
     */
    protected abstract Path resolveWebapppDirectory();

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public String getContextName() {
        return this.contextName;
    }
    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public Path getContextConfigurationFile() {
        return this.contextConfigurationFile;
    }
    public void setContextConfigurationFile(Path contextConfigurationFile) {
        this.contextConfigurationFile = contextConfigurationFile;
    }

}