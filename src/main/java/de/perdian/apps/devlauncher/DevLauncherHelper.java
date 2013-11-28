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

/**
 * Global helper methods
 *
 * @author Christian Robert
 */

public class DevLauncherHelper {

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

}