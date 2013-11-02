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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DevLauncherShutdownListener {

    static final String SHUTDOWN_COMMAND = "shutdown";
    static final String SHUTDOWN_CONFIRMATION = "shutdownConfirmation";

    static final Logger log = LoggerFactory.getLogger(DevLauncherShutdownListener.class);
    static final Lock shutdownLock = new ReentrantLock();

    /**
     * Shutdown an already running server
     *
     * @param shutdownPort
     *   the port on which the connection to the already running server will be
     *   initiated
     */
    static void shutdownExistingServer(Integer shutdownPort) {
        if(shutdownPort != null) {
            DevLauncherShutdownListener.log.debug("Try shutting down running server using port: " + shutdownPort);
            try {
                Socket shutdownSocket = new Socket();
                try {
                    shutdownSocket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), shutdownPort.intValue()), 100);
                    BufferedWriter shutdownWriter = new BufferedWriter(new OutputStreamWriter(shutdownSocket.getOutputStream(), "UTF-8"));
                    try {

                        // Send the shutdown command
                        shutdownWriter.write(DevLauncherShutdownListener.SHUTDOWN_COMMAND + "\n");
                        shutdownWriter.flush();
                        DevLauncherShutdownListener.log.debug("Shutdown command successfully sent to running server");

                        // Wait for response
                        BufferedReader confirmationReader = new BufferedReader(new InputStreamReader(shutdownSocket.getInputStream(), "UTF-8"));
                        try {
                            for(String line = confirmationReader.readLine(); line != null; line = confirmationReader.readLine()) {
                                if(DevLauncherShutdownListener.SHUTDOWN_CONFIRMATION.equals(line)) {
                                    DevLauncherShutdownListener.log.debug("Previous server instance confirmed shutdown");
                                }
                            }
                        } catch(Exception e) {
                            DevLauncherShutdownListener.log.debug("No response from server that was to be shutdown could be received - it may be shutdown, it may not [" + e + "]");
                        } finally {
                            confirmationReader.close();
                        }

                    } finally {
                        shutdownWriter.close();
                    }
                } finally {
                    shutdownSocket.close();
                }
            } catch(Exception e) {
                DevLauncherShutdownListener.log.debug("No running server detected or server could not be shutdown [" + e + "]");
            }
        }
    }

    /**
     * Installs the listener on a running tomcat instance.
     *
     * @param tomcat
     *   the tomcat instance that will be shutdown once a connection from a new
     *   application instance is received
     * @param shutdownPort
     *   the port on which the server will listen to new clients that want to
     *   initiate a shutdown
     */
    static void installForServer(final Tomcat tomcat, final Integer shutdownPort) throws Exception {
        if(shutdownPort != null) {

            // Start a daemon thread that listens on the shutdown port for
            // incoming connections. Whenever there actually is a connection
            // sending the shutdown command, we - well - shutdown the system
            // by trying a clean stop of the embedded server and then performing
            // a System.exit call to terminate the virtual machine.
            Thread shutdownThread = new Thread(new Runnable() {
                @Override public void run() {
                    log.info("Start listening for shutdown commands on port: " + shutdownPort);
                    try {
                        ServerSocket serverSocket = new ServerSocket(shutdownPort.intValue(), 0, InetAddress.getByName("localhost"));
                        try {
                            while(serverSocket.isBound()) {
                                Socket clientSocket = serverSocket.accept();
                                try {
                                    DevLauncherShutdownListener.handleShutdownConnection(clientSocket, tomcat);
                                } catch(Exception e) {
                                    DevLauncherShutdownListener.log.trace("Cannot accept shutdown socket connection", e);
                                } finally {
                                    clientSocket.close();
                                }
                            }
                        } finally {
                            serverSocket.close();
                        }
                    } catch(Exception e) {
                        DevLauncherShutdownListener.log.debug("Cannot install shutdown listener on port: " + shutdownPort, e);
                    }
                }
            });
            shutdownThread.setDaemon(true);
            shutdownThread.setName(DevLauncherShutdownListener.class.getSimpleName() + "[" + shutdownPort + "]");
            shutdownThread.start();

            tomcat.getServer().await();
            DevLauncherShutdownListener.log.info("Embedded webserver has been stopped - exiting application");
            DevLauncherShutdownListener.shutdownLock.lock();
            try {
                System.exit(0);
            } finally {
                DevLauncherShutdownListener.shutdownLock.unlock();
            }

        }
    }

    static void handleShutdownConnection(Socket clientSocket, Tomcat tomcat) throws Exception {
        BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        for(String clientLine = clientReader.readLine(); clientLine != null; clientLine = clientReader.readLine()) {
            if(DevLauncherShutdownListener.SHUTDOWN_COMMAND.equalsIgnoreCase(clientLine)) {
                DevLauncherShutdownListener.shutdownLock.lock();
                try {

                    DevLauncherShutdownListener.log.info("Shutdown command received - Stopping embedded webserver");
                    try {
                        DevLauncherShutdownListener.handleShutdownServer(tomcat);
                    } finally {
                        BufferedWriter confirmationWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                        try {
                            confirmationWriter.write(DevLauncherShutdownListener.SHUTDOWN_CONFIRMATION + "\n");
                            confirmationWriter.flush();
                        } catch(Exception e) {
                            DevLauncherShutdownListener.log.debug("Could not send shutdown confirmation command", e);
                        } finally {
                            confirmationWriter.close();
                        }
                    }

                } catch(Exception e) {
                    DevLauncherShutdownListener.log.error("Cannot stop embedded webserver correctly - using System.exit to force shutdown", e);
                    System.exit(-1);
                } finally {
                    DevLauncherShutdownListener.shutdownLock.unlock();
                }
            }
        }
    }

    static void handleShutdownServer(Tomcat tomcat) throws Exception {
        tomcat.getServer().stop();
    }

}