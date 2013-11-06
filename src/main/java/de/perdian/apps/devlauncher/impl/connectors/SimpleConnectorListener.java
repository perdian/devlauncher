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
package de.perdian.apps.devlauncher.impl.connectors;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Adds a new connector to the running tomcat instance
 *
 * @author Christian Robert
 */

public class SimpleConnectorListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(SimpleConnectorListener.class);
    private int myPort = -1;
    private String myProtocol = null;
    private int myRedirectPort = -1;

    public SimpleConnectorListener(int port) {
        this(null, port, -1);
    }

    public SimpleConnectorListener(String protocol, int port, int redirectPort) {
        this.setRedirectPort(redirectPort);
        this.setPort(port);
        this.setProtocol(protocol);
    }

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) throws Exception {

        Connector connector = this.createConnector();
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Adding simple connector");
        if(this.getProtocol() != null) {
            logMessage.append(" for protocol '").append(this.getProtocol());
        }
        logMessage.append(" listening on port ").append(this.getPort());
        if(this.getRedirectPort() > 0) {
            logMessage.append(" and redirectPort ").append(this.getRedirectPort());
        }
        logMessage.append(" [").append(connector).append("]");
        log.debug(logMessage.toString());

        tomcat.getService().addConnector(connector);

    }

    protected Connector createConnector() {
        Connector connector = new Connector(this.getProtocol());
        connector.setPort(this.getPort());
        if(this.getRedirectPort() > 0) {
            connector.setRedirectPort(this.getRedirectPort());
        }
        return connector;
    }

    // -------------------------------------------------------------------------
    // ---  Property access methods  -------------------------------------------
    // -------------------------------------------------------------------------

    private int getPort() {
        return this.myPort;
    }
    private void setPort(int port) {
        this.myPort = port;
    }

    private String getProtocol() {
        return this.myProtocol;
    }
    private void setProtocol(String protocol) {
        this.myProtocol = protocol;
    }

    private int getRedirectPort() {
        return this.myRedirectPort;
    }
    private void setRedirectPort(int redirectPort) {
        this.myRedirectPort = redirectPort;
    }

}