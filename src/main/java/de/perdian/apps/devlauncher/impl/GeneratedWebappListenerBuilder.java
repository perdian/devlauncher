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
package de.perdian.apps.devlauncher.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * Provides convenient methods to create a {@code GeneratedWebappListener}
 *
 * @author Christian Robert
 */

public class GeneratedWebappListenerBuilder {

    private List<GeneratedWebappCopyDefinition> copyDefinitions = new ArrayList<>();
    private Path targetDirectory = null;

    /**
     * Creates the listener configured with the internal properties
     */
    public GeneratedWebappListener createListener() throws IOException {
        GeneratedWebappListener listener = this.createListenerInstance();
        listener.setTargetDirectory(this.getTargetDirectory());
        listener.setCopyDefinitions(this.getCopyDefinitions());
        return listener;
    }

    protected GeneratedWebappListener createListenerInstance() {
        return new GeneratedWebappListener();
    }


    // -------------------------------------------------------------------------
    // --- Copy definition -----------------------------------------------------
    // -------------------------------------------------------------------------

    public GeneratedWebappListenerBuilder addCopyDefinition(Path sourceDirectory) {
        return this.addCopyDefinition(sourceDirectory, null, null);
    }

    public GeneratedWebappListenerBuilder addCopyDefinition(Path sourceDirectory, Predicate<Path> fileFilter) {
        return this.addCopyDefinition(sourceDirectory, null, fileFilter);
    }

    public GeneratedWebappListenerBuilder addCopyDefinition(Path sourceDirectory, String targetDirectoryName) {
        return this.addCopyDefinition(sourceDirectory, targetDirectoryName, null);
    }

    public GeneratedWebappListenerBuilder addCopyDefinition(Path sourceDirectory, String targetDirectoryName, Predicate<Path> fileFilter) {
        GeneratedWebappCopyDefinition copyDefinition = new GeneratedWebappCopyDefinition();
        copyDefinition.setSourceDirectory(sourceDirectory);
        copyDefinition.setTargetDirectoryName(targetDirectoryName);
        copyDefinition.setFileFilter(fileFilter);
        this.getCopyDefinitions().add(copyDefinition);
        return this;

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

    public GeneratedWebappListenerBuilder targetDirectory(Path targetDirectory) {
        this.setTargetDirectory(targetDirectory);
        return this;
    }
    public Path getTargetDirectory() {
        return this.targetDirectory;
    }
    public void setTargetDirectory(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}