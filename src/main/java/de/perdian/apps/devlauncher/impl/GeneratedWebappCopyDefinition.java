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

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Defines a location from which the files are copied into the target
 * web application directory
 *
 * @author Christian Robert
 */
public class GeneratedWebappCopyDefinition {

    private Path sourceDirectory = null;
    private Predicate<Path> fileFilter = null;
    private String targetDirectoryName = null;

    // ---------------------------------------------------------------------
    // --- Property access methods -----------------------------------------
    // ---------------------------------------------------------------------

    /**
     * Gets the source directory from where the files will be read and
     * copied to the target directory/directories.
     */
    public Path getSourceDirectory() {
        return this.sourceDirectory;
    }
    public void setSourceDirectory(Path sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * Gets the filter that specifies which of the files within
     * the source directory will be copied
     */
    public Predicate<Path> getFileFilter() {
        return this.fileFilter;
    }
    public void setFileFilter(Predicate<Path> fileFilter) {
        this.fileFilter = fileFilter;
    }

    /**
     * Gets the name of the target directories (below the webapp directory
     * into which the files will be written)
     */
    public String getTargetDirectoryName() {
        return this.targetDirectoryName;
    }
    public void setTargetDirectoryName(String targetDirectoryName) {
        this.targetDirectoryName = targetDirectoryName;
    }

}