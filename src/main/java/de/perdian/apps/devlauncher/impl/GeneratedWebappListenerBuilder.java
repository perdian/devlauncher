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
package de.perdian.apps.devlauncher.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Provides convenient methods to create a {@code GeneratedWebappListener}
 *
 * @author Christian Robert
 */

public class GeneratedWebappListenerBuilder {

    private List<GeneratedWebappCopyDefinition> copyDefinitions = new ArrayList<>();
    private File targetDirectory = null;

    /**
     * Creates the listener configured with the internal properties
     */
    public GeneratedWebappListener createListener() throws IOException {
        GeneratedWebappListener listener = this.createListenerInstance();
        listener.setCopyDefinitions(this.getCopyDefinitions());
        return listener;
    }

    protected GeneratedWebappListener createListenerInstance() {
        return new GeneratedWebappListener();
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public List<GeneratedWebappCopyDefinition> getCopyDefinitions() {
        return this.copyDefinitions;
    }
    public void setCopyDefinitions(List<GeneratedWebappCopyDefinition> copyDefinitions) {
        this.copyDefinitions = copyDefinitions;
    }

    public File getTargetDirectory() {
        return this.targetDirectory;
    }
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}