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

import java.util.Map;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central executor class that starts your Web Application.
 *
 * @author Christian Robert
 */

public class DevLauncher {

  private static final Logger log = LoggerFactory.getLogger(DevLauncher.class);

  private String myContextPath = null;
  private String myWebappDirectory = null;
  private int myHttpPort = 8080;
  private Properties myWebappInitParameters = null;
  private int myShutdownListenerPort = 9080;

  /**
   * Launches the embedded webserver and make your application available
   */
  public void launch() {
    if(this.getWebappDirectory() == null || this.getWebappDirectory().length() <= 0) {
      throw new IllegalArgumentException("Property 'webappDirectory' must not be null or empty!");
    } else {

      DevShutdownListener.shutdownExistingServer(this.getShutdownListenerPort());

      log.trace("Configuration DevLauncher");
      WebAppContext webAppContext = this.createWebAppContext();

      try {

        log.info("Launching embedded Jetty webserver");
        Server server = new Server(this.getHttpPort());
        server.setHandler(webAppContext);
        server.start();
        log.info("Embedded Jetty webserver launched");

        DevShutdownListener.installForServer(server, this.getShutdownListenerPort());

      } catch(Exception e) {
        throw new RuntimeException("Cannot launch embedded webserver", e);
      }

    }
  }

  protected WebAppContext createWebAppContext() {
    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setContextPath(this.getContextPath() == null ? "/" : this.getContextPath());
    webAppContext.setResourceBase(this.getWebappDirectory());
    Properties initParameters = this.getWebappInitParameters();
    if(initParameters != null) {
      for(Map.Entry<?, ?> initParameter : initParameters.entrySet()) {
        webAppContext.setInitParameter((String)initParameter.getKey(), (String)initParameter.getValue());
      }
    }
    return webAppContext;
  }

  // ---------------------------------------------------------------------------
  // --- Property access methods -----------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Gets the path under which your application will be made available in the
   * servers URL. For example a {@code contextPath} of {@code foo} will result
   * in your application being made available within the servlet container at:
   * {@code http://localhost:8080/foo/}
   */
  public String getContextPath() {
    return this.myContextPath;
  }
  public void setContextPath(String contextPath) {
    this.myContextPath = contextPath;
  }

  /**
   * Gets directory under which your Web Application resources can be found.
   * This is the directory under which the {@code WEB-INF} directory and all
   * other resources must be located
   */
  public String getWebappDirectory() {
    return this.myWebappDirectory;
  }
  public void setWebappDirectory(String webappDirectory) {
    this.myWebappDirectory = webappDirectory;
  }

  /**
   * Gets additional parameters that are added to the Web Application context
   * and can later be retreived using the {@code ServletContext} object
   */
  public Properties getWebappInitParameters() {
    return this.myWebappInitParameters;
  }
  public void setWebappInitParameters(Properties webappInitParameters) {
    this.myWebappInitParameters = webappInitParameters;
  }
  public void addWebappInitParameter(String parameterName, String parameterValue) {
    Properties initParameters = this.getWebappInitParameters();
    if(initParameters == null) {
      initParameters = new Properties();
      this.setWebappInitParameters(initParameters);
    }
    initParameters.setProperty(parameterName, parameterValue);
  }

  /**
   * Gets the port under which the embedded webserver will listen for requests
   */
  public int getHttpPort() {
    return this.myHttpPort;
  }
  public void setHttpPort(int httpPort) {
    this.myHttpPort = httpPort;
  }

  /**
   * Gets the port under which the shutdown listener is started. If this value
   * is {@code 0} or less, then the shutdown listener machanismn will be
   * disabled.
   */
  public int getShutdownListenerPort() {
    return this.myShutdownListenerPort;
  }
  public void setShutdownListenerPort(int shutdownListenerPort) {
    this.myShutdownListenerPort = shutdownListenerPort;
  }

}