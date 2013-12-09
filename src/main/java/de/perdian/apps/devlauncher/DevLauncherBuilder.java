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
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the defult configuration by evaluating the system properties for
 * any settings that customize the launching process
 *
 * @author Christian Robert
 */

public class DevLauncherBuilder {

    private final Logger log = LoggerFactory.getLogger(DevLauncherBuilder.class);

    private String myWorkingDirectoryName = null;

    public DevLauncherBuilder() {
        this(".devlauncher");
    }

    public DevLauncherBuilder(String workingDirectoryName) {
        this.setWorkingDirectoryName(workingDirectoryName);
    }

    /**
     * Creates a new configuration from the given configuration file
     *
     * @return
     *   the created configuration
     * @throws IOException
     *   thrown if the configuration cannot be read correctly
     */
    public DevLauncher createLauncher() throws IOException {
        return this.createLauncher(null);
    }

    /**
     * Creates a new configuration from the given configuration file
     *
     * @param configurationFile
     *   the configuration file, which is expected to contain properties in the
     *   default Java properties format.
     * @return
     *   the created configuration
     * @throws IOException
     *   thrown if the configuration cannot be read correctly
     */
    public DevLauncher createLauncher(File configurationFile) throws IOException {

        DevLauncherHelper.loadConfigurationFile(configurationFile);

        String defaultPortValue = System.getProperty("devlauncher.defaultPort", "8080");
        String shutdownPortValue = System.getProperty("devlauncher.shutdownPort", "8081");

        DevLauncher launcher = new DevLauncher();
        launcher.setDefaultPort(defaultPortValue == null || defaultPortValue.length() <= 0 ? null : Integer.valueOf(defaultPortValue));
        launcher.setShutdownPort(shutdownPortValue == null  || shutdownPortValue.length() <= 0 ? null : Integer.valueOf(shutdownPortValue));
        launcher.setWorkingDirectory(this.resolveWorkingDirectory());
        return launcher;

    }

    private File resolveWorkingDirectory() throws IOException {
        String workingDirectoryValue = System.getProperty("devlauncher.workingDirectory", null);
        File workingDirectory = workingDirectoryValue != null && workingDirectoryValue.length() > 0 ? new File(workingDirectoryValue).getCanonicalFile() : null;
        if(workingDirectory == null) {
            workingDirectory = this.resolveDefaultWorkingDirectory();
        }
        if(!workingDirectory.exists()) {
            this.log.debug("Creating devlauncher working directory at: " + workingDirectory.getAbsolutePath());
            workingDirectory.mkdirs();
        }
        return workingDirectory;
    }

    private File resolveDefaultWorkingDirectory() throws IOException {
        String workingDirectoryName = this.getWorkingDirectoryName();
        File userHomeDirectory = new File(System.getProperty("user.home")).getCanonicalFile();
        File workingDirectory = new File(userHomeDirectory, workingDirectoryName != null ? workingDirectoryName : System.getProperty("devlauncher.workingDirectoryName", ".devlauncher"));
        return workingDirectory;
    }

    // -------------------------------------------------------------------------
    // ---  Property access methods  -------------------------------------------
    // -------------------------------------------------------------------------

    /**
     * Gets the name of the working directory for the created launcher
     */
    public String getWorkingDirectoryName() {
        return this.myWorkingDirectoryName;
    }
    public void setWorkingDirectoryName(String workingDirectoryName) {
        this.myWorkingDirectoryName = workingDirectoryName;
    }

}