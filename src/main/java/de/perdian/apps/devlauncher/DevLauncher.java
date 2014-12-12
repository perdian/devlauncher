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
package de.perdian.apps.devlauncher;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevLauncher {

    private static final Logger log = LoggerFactory.getLogger(DevLauncher.class);

    private Integer defaultPort = Integer.valueOf(8080);
    private Integer shutdownPort = Integer.valueOf(8081);
    private Path workingDirectory = null;
    private List<DevLauncherListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new {@code DevLauncher} instance
     *
     * @param workingDirectory
     *     the working directory in which to store the temporary information
     */
    public DevLauncher(Path workingDirectory) {
        this.setWorkingDirectory(workingDirectory);
    }

    /**
     * Launches the internal webserver and initialize the server according to
     * the internal list of {@link DevLauncherListener} instances
     *
     * @throws Exception
     *     thrown if any kind of error occures during the server start
     */
    public void launch() throws Exception {

        DevLauncherShutdownListener.shutdownExistingServer(this.getShutdownPort());

        // No create and configure the embedded tomcat webserver
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(this.getWorkingDirectory().resolve("tomcat/").toFile().getCanonicalPath());
        tomcat.setPort(this.getDefaultPort());
        tomcat.enableNaming();

        log.trace("Invoking DevLauncherListener instances");
        for (DevLauncherListener listener : this.getListeners()) {
            listener.customizeServer(tomcat, this);
        }

        log.info("Starting embedded webserver");
        tomcat.start();

        log.trace("Waiting for server shutdown");
        DevLauncherShutdownListener.installForServer(tomcat, this.getShutdownPort());
        tomcat.getServer().await();

    }

    // -------------------------------------------------------------------------
    // --- Property access methods -------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Gets the default port on which the embedded werserver will listen to
     * incoming requests
     */
    public Integer getDefaultPort() {
        return this.defaultPort;
    }
    public void setDefaultPort(Integer defaultPort) {
        this.defaultPort = defaultPort;
    }

    /**
     * Gets the working directory in which the running webserver instance can
     * store it's temporary data and other relevant files
     */
    public Path getWorkingDirectory() {
        return this.workingDirectory;
    }
    public void setWorkingDirectory(Path workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the port on which the launcher will listen for a shutdown event
     */
    public Integer getShutdownPort() {
        return this.shutdownPort;
    }
    public void setShutdownPort(Integer shutdownPort) {
        this.shutdownPort = shutdownPort;
    }

    /**
     * Gets all the listeners interacting with this launcher
     */
    List<DevLauncherListener> getListeners() {
        return this.listeners;
    }
    void setListeners(List<DevLauncherListener> listeners) {
        this.listeners = listeners;
    }
    public void addListener(DevLauncherListener listener) {
        this.getListeners().add(listener);
    }

}
