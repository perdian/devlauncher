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
package de.perdian.apps.devlauncher.impl.webapps;

import java.io.File;
import java.io.IOException;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Appends a new web application to the tomcat instance
 *
 * @author Christian Robert
 */

public abstract class AbstractWebappListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebappListener.class);
    private String myWebappName = null;

    public AbstractWebappListener(String webappName) {
        this.setWebappName(webappName);
    }

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) throws Exception {

        File webappDirectory = this.resolveWebappDirectory(launcher);
        log.debug("Resolved webapp directory for webapp '" + this.getWebappName() + "' to: " + webappDirectory.getAbsolutePath());

        Context webappContext = tomcat.addWebapp("/" + this.getWebappName(), webappDirectory.getCanonicalPath());

        File contextConfigurationFile = this.resolveContextConfigurationFile(launcher);
        if(contextConfigurationFile != null && contextConfigurationFile.exists()) {
            log.debug("Resolved context file for webapp '" + this.getWebappName() + "' to: " + contextConfigurationFile.getAbsolutePath());
            webappContext.setConfigFile(contextConfigurationFile.toURI().toURL());
        }

    }

    protected abstract File resolveWebappDirectory(DevLauncher launcher) throws IOException;

    protected File resolveContextConfigurationFile(DevLauncher launcher) throws IOException {
        return null;
    }

    // -------------------------------------------------------------------------
    // ---  Property access methods  -------------------------------------------
    // -------------------------------------------------------------------------

    protected String getWebappName() {
        return this.myWebappName;
    }
    private void setWebappName(String webappName) {
        this.myWebappName = webappName;
    }

}