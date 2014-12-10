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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global helper methods
 *
 * @author Christian Robert
 */

public class DevLauncherHelper {

    private static final Logger log = LoggerFactory.getLogger(DevLauncherHelper.class);

    static File resolveWorkingDirectory(String workingDirectoryName) {
        try {
            String workingDirectoryValue = System.getProperty("devlauncher.workingDirectory", null);
            File workingDirectory = workingDirectoryValue != null && workingDirectoryValue.length() > 0 ? new File(workingDirectoryValue).getCanonicalFile() : null;
            if (workingDirectory == null) {
                File userHomeDirectory = new File(System.getProperty("user.home")).getCanonicalFile();
                workingDirectory = new File(userHomeDirectory, workingDirectoryName != null ? workingDirectoryName : System.getProperty("devlauncher.workingDirectoryName", ".devlauncher"));
            }
            if (!workingDirectory.exists()) {
                log.debug("Creating devlauncher working directory at: " + workingDirectory.getAbsolutePath());
                workingDirectory.mkdirs();
            }
            return workingDirectory;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot resolve working directory", e);
        }
    }

}