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

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DevShutdownListener {

  static final String SHUTDOWN_COMMAND = "shutdown";
  static final String SHUTDOWN_CONFIRMATION = "shutdownConfirmation";

  static final Logger log = LoggerFactory.getLogger(DevShutdownListener.class);

  static void shutdownExistingServer(int shutdownPort) {
    if(shutdownPort > 0) {
      log.debug("Try shutting down running server");
      try {
        try(Socket shutdownSocket = new Socket()) {
          shutdownSocket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), shutdownPort), 100);
          try(BufferedWriter shutdownWriter = new BufferedWriter(new OutputStreamWriter(shutdownSocket.getOutputStream(), "UTF-8"))) {

            // Send the shutdown command
            shutdownWriter.write(SHUTDOWN_COMMAND + "\n");
            shutdownWriter.flush();
            log.debug("Shutdown command successfully sent to running server");

            // Wait for response
            try(BufferedReader confirmationReader = new BufferedReader(new InputStreamReader(shutdownSocket.getInputStream(), "UTF-8"))) {
              for(String line = confirmationReader.readLine(); line != null; line = confirmationReader.readLine()) {
                if(SHUTDOWN_CONFIRMATION.equals(line)) {
                  log.debug("Previous server instance confirmed shutdown");
                }
              }
            } catch(Exception e) {
              log.debug("No response from server that was to be shutdown could be received - it may be shutdown, it may not [" + e + "]");
            }

          }
        }
      } catch(Exception e) {
        log.debug("No running server detected or server could not be shutdown [" + e + "]");
      }
    }
  }

  static void installForServer(final Server server, final int shutdownPort) throws Exception {
    if(shutdownPort > 0) {

      // Start a deamon thrad that listens on the shutdown port for incoming
      // connections. Whenever there actually is a connection sending the shutdown
      // command, we - well - shutdown the system by trying a clean stop of the
      // embedded Jetty server and then performing a System.exit call to terminate
      // the virtual machine.
      Thread shutdownThread = new Thread(new Runnable() {
        @Override public void run() {
          try {
            try(ServerSocket serverSocket = new ServerSocket(shutdownPort, 0, InetAddress.getByName("localhost"))) {
              while(serverSocket.isBound()) {
                try(Socket clientSocket = serverSocket.accept()) {
                  DevShutdownListener.handleShutdownConnection(clientSocket, server);
                } catch(Exception e) {
                  log.trace("Cannot accept shutdown socket connection", e);
                }
              }
            }
          } catch(Exception e) {
            log.debug("Cannot install shutdown listener on port: " + shutdownPort, e);
          }
        }
      });
      shutdownThread.setDaemon(true);
      shutdownThread.setName(DevShutdownListener.class.getSimpleName() + "[" + shutdownPort + "]");
      shutdownThread.start();

      server.join();
      log.info("Embedded Jetty webserver has been stopped - exiting application");
      System.exit(0);

    }
  }

  static void handleShutdownConnection(Socket clientSocket, Server server) throws Exception {
    BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
    for(String clientLine = clientReader.readLine(); clientLine != null; clientLine = clientReader.readLine()) {
      if(SHUTDOWN_COMMAND.equalsIgnoreCase(clientLine)) {
        try {

          log.info("Shutdown command received - Stopping embedded Jetty webserver");
          try {
            server.stop();
          } finally {
            try(BufferedWriter confirmationWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"))) {
              confirmationWriter.write(SHUTDOWN_CONFIRMATION + "\n");
              confirmationWriter.flush();
            } catch(Exception e) {
              log.debug("Could not send shutdown confirmation command", e);
            }
          }

        } catch(Exception e) {
          log.error("Cannot stop embedded Jetty webserver correctly - using System.exit to force shutdown", e);
          System.exit(-1);
        }
      }
    }
  }

}