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
package de.perdian.apps.devlauncher;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevLauncher {

    private static final Logger log = LoggerFactory.getLogger(DevLauncher.class);

    private Integer myDefaultPort = 0;
    private Integer myShutdownPort = null;
    private File myWorkingDirectory = null;
    private List<DevLauncherListener> myListeners = new CopyOnWriteArrayList<DevLauncherListener>();

    /**
     * Launches the internal webserver
     */
    public void launch() throws Exception {

        DevLauncherShutdownListener.shutdownExistingServer(this.getShutdownPort());

        // To make sure the JSP context is available, we have to access the
        // class at least once. This basically is a dirty hack to allow Tiles 3
        // to work together with a Spring context defined through the
        // initializer before any servlet instance has been created
        try { Class.forName("org.apache.jasper.compiler.JspRuntimeContext"); } catch(Exception e) {};

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(new File(this.getWorkingDirectory(), "tomcat/").getCanonicalPath());
        tomcat.setPort(this.getDefaultPort());
        tomcat.enableNaming();

        // Make sure all listeners get to do their work
        for(DevLauncherListener listener : this.getListeners()) {
            listener.customizeServer(tomcat, this);
        }

        log.info("Starting embedded webserver");
        tomcat.start();

        // Wait for shutdown
        DevLauncherShutdownListener.installForServer(tomcat, this.getShutdownPort());
        tomcat.getServer().await();

    }

    // -------------------------------------------------------------------------
    // ---  Property access methods  -------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Gets the default port on which the embedded werserver will listen to
     * incoming requests
     */
    public Integer getDefaultPort() {
        return this.myDefaultPort;
    }
    public void setDefaultPort(Integer defaultPort) {
        this.myDefaultPort = defaultPort;
    }

    /**
     * Gets the working directory in which the running webserver instance can
     * store it's temporary data and other relevant files
     */
    public File getWorkingDirectory() {
        return this.myWorkingDirectory;
    }
    public void setWorkingDirectory(File workingDirectory) {
        this.myWorkingDirectory = workingDirectory;
    }

    /**
     * Gets the port on which the launcher will listen for a shutdown event
     */
    public Integer getShutdownPort() {
        return this.myShutdownPort;
    }
    public void setShutdownPort(Integer shutdownPort) {
        this.myShutdownPort = shutdownPort;
    }

    /**
     * Gets all the listeners interacting with this launcher
     */
    List<DevLauncherListener> getListeners() {
        return this.myListeners;
    }
    void setListeners(List<DevLauncherListener> listeners) {
        this.myListeners = listeners;
    }
    public void addListener(DevLauncherListener listener) {
        this.getListeners().add(listener);
    }

}
