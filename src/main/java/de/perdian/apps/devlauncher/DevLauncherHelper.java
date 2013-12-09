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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global helper methods
 *
 * @author Christian Robert
 */

public class DevLauncherHelper {

    private static final Logger log = LoggerFactory.getLogger(DevLauncherHelper.class);

    public static File resolveProjectDirectory() throws IOException {
        String projectDirectoryValue = System.getProperty("devlauncher.projectDirectory", null);
        File projectDirectory = projectDirectoryValue == null ? null : new File(projectDirectoryValue);
        if(projectDirectory == null) {
            return new File(".").getCanonicalFile();
        } else {
            return projectDirectory.getCanonicalFile();
        }
    }

    public static File resolveConfigurationFile() throws IOException {
        File projectDirectory = DevLauncherHelper.resolveProjectDirectory();
        String configurationFileValue = System.getProperty("devlauncher.configurationFile", "devlauncher.properties");
        File configurationFile = new File(configurationFileValue);
        if(configurationFile.isAbsolute()) {
            return configurationFile;
        } else {
            return new File(projectDirectory, configurationFileValue);
        }
    }

    public static void loadConfigurationFile() {
        DevLauncherHelper.loadConfigurationFile(null);
    }

    public static void loadConfigurationFile(File configurationFile) {
        try {
            File useConfigurationFile = configurationFile == null ? DevLauncherHelper.resolveConfigurationFile() : configurationFile;
            if(useConfigurationFile != null) {
                if(!useConfigurationFile.exists()) {
                    log.info("No devlauncher configuration file found at: " + useConfigurationFile.getAbsolutePath() + ". Using default settings.");
                } else {
                    log.info("Loading devlauncher configuration from: " + useConfigurationFile.getAbsolutePath());
                    Properties configurationProperties = new Properties();
                    try {
                        InputStream configurationStream = new BufferedInputStream(new FileInputStream(useConfigurationFile));
                        try {
                            configurationProperties.load(configurationStream);
                        } finally {
                            configurationStream.close();
                        }
                    } catch(Exception e) {
                        log.warn("Cannot load devlauncher configuration properties from: " + useConfigurationFile.getAbsolutePath(), e);
                    }
                    for(Map.Entry<Object, Object> configurationEntry : configurationProperties.entrySet()) {
                        String configurationKey = (String)configurationEntry.getKey();
                        if(System.getProperty(configurationKey, null) == null) {
                            System.setProperty(configurationKey, (String)configurationEntry.getValue());
                        }
                    }
                }
            }
        } catch(IOException e) {
            throw new RuntimeException("Cannot load configuration file", e);
        }
    }

}