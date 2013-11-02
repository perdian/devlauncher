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

import de.perdian.apps.devlauncher.DevLauncher;

public class SimpleWebappListener extends AbstractWebappListener {

    private File myWebappDirectory = null;

    public SimpleWebappListener(String webappName, File webappDirectory) {
        super(webappName);
        this.setWebappDirectory(webappDirectory);
    }

    @Override
    protected File resolveWebappDirectory(DevLauncher launcher) {
        return this.getWebappDirectory();
    }

    // -------------------------------------------------------------------------
    // ---  Property access methods  -------------------------------------------
    // -------------------------------------------------------------------------

    private File getWebappDirectory() {
        return this.myWebappDirectory;
    }
    private void setWebappDirectory(File webappDirectory) {
        this.myWebappDirectory = webappDirectory;
    }

}