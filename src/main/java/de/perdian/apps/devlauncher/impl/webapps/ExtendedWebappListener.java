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
import java.io.FileNotFoundException;
import java.io.IOException;

import de.perdian.apps.devlauncher.DevLauncher;

public class ExtendedWebappListener extends AbstractWebappListener {

    public ExtendedWebappListener(String webappName) {
        super(webappName);
    }

    @Override
    protected final File resolveWebappDirectory(DevLauncher launcher) throws IOException {
        File projectDirectory = this.resolveProjectDirectory();
        return this.resolveWebappDirectory(projectDirectory, launcher.getWorkingDirectory());
    }

    @Override
    protected final File resolveContextConfigurationFile(DevLauncher launcher) throws IOException {
        File projectDirectory = this.resolveProjectDirectory();
        return this.resolveContextConfigurationFile(projectDirectory, launcher.getWorkingDirectory());
    }

    protected File resolveContextConfigurationFile(File projectDirectory, File workingDirectory) throws IOException {
        return null;
    }

    protected File resolveWebappDirectory(File projectDirectory, File workingDirectory) throws IOException {
        File webappDirectory = new File(projectDirectory, "src/main/webapp");
        if(!webappDirectory.exists()) {
            throw new FileNotFoundException("Cannot find webapp directory for webapp '" + this.getWebappName() + "' at: " + webappDirectory.getAbsolutePath());
        } else {
            return webappDirectory;
        }
    }

    private File resolveProjectRootDirectory() throws IOException {
        String projectRootDirectoryValue = System.getProperty("devlauncher.projectRootDirectory", null);
        File projectRootDirectory = projectRootDirectoryValue != null && projectRootDirectoryValue.length() > 0 ? new File(projectRootDirectoryValue).getCanonicalFile() : null;
        if(projectRootDirectory == null) {
            return this.resolveCurrentProjectDirectory().getParentFile();
        } else {
            return projectRootDirectory;
        }
    }

    private File resolveCurrentProjectDirectory() throws IOException {
        String projectDirectoryValue = System.getProperty("devlauncher.projectDirectory", null);
        File projectDirectory = projectDirectoryValue == null ? null : new File(projectDirectoryValue);
        if(projectDirectory == null) {
            return new File(".").getCanonicalFile();
        } else {
            return projectDirectory.getCanonicalFile();
        }
    }

    private File resolveProjectDirectory() throws IOException {
        String projectDirectoryValue = System.getProperty("devlauncher.project." + this.getWebappName(), null);
        File projectDirectory = projectDirectoryValue == null ? null : new File(projectDirectoryValue).getCanonicalFile();
        if(projectDirectory == null) {
            File projectRootDirectory = this.resolveProjectRootDirectory();
            projectDirectory = new File(projectRootDirectory, this.getWebappName());
        }
        if(!projectDirectory.exists()) {
            throw new FileNotFoundException("Cannot find project directory for project '" + this.getWebappName() + "' at: " + projectDirectory.getAbsolutePath());
        }
        return projectDirectory;
    }

}