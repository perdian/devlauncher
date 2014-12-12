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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a valid web application within a temporary directory from a set
 * of source directories. This works a little bit like a Maven overlay, by
 * copying all the resources required for the web application into the target
 * directory before initializing the context.
 *
 * Any changes to files within the source directories will be recognized and
 * the updated files will be copied into the target web application directory.
 *
 * @author Christian Robert
 */

public class GeneratedWebappListener extends WebappListener {

    private static final Logger log = LoggerFactory.getLogger(GeneratedWebappListener.class);

    private Path targetDirectory = null;
    private List<GeneratedWebappCopyDefinition> copyDefinitions = new CopyOnWriteArrayList<>();

    public GeneratedWebappListener(String contextName) {
        super(contextName);
    }

    @Override
    protected Path resolveWebappDirectory() throws IOException {
        if (this.getTargetDirectory() == null) {
            throw new IllegalArgumentException("Parameter 'targetDirectory' not set!");
        } else if (!Files.exists(this.getTargetDirectory().getParent())) {
            try {
                log.debug("Creating web application target directory at: {}", this.getTargetDirectory().getParent());
                Files.createDirectories(this.getTargetDirectory().getParent());
            } catch (IOException e) {
                throw new RuntimeException("Cannot create target directory at: " + this.getTargetDirectory().getParent());
            }
        }
        return this.getTargetDirectory();
    }

    @Override
    protected Context createWebappContext(Tomcat tomcat, Path webappDirectory) {

        // Make sure the target content has been added to the target directory
        this.initializeCopyDefinitions(tomcat);

        // Continue with the registration
        return super.createWebappContext(tomcat, webappDirectory);

    }

    /**
     * Make sure the source directories and the target directories are in sync
     */
    protected void initializeCopyDefinitions(Tomcat tomcat) {
        if (!this.getCopyDefinitions().isEmpty()) {
            try {
                log.info("Synchronizing {} copy definitions", this.getCopyDefinitions().size());
                for (GeneratedWebappCopyDefinition copyDefinition : this.getCopyDefinitions()) {
                    this.initializeCopyDefinition(copyDefinition, tomcat);
                }
                log.info("Completed synchronizing {} copy definitions", this.getCopyDefinitions().size());
            } catch (IOException e) {
                throw new RuntimeException("Cannot copy resources into target directory: " + this.getTargetDirectory(), e);
            }
        }
    }

    /**
     * Make sure the source directories and the target directories are in sync
     */
    protected void initializeCopyDefinition(GeneratedWebappCopyDefinition copyDefinition, Tomcat tomcat) throws IOException {

        // Now add a change listener so that whenever a file will change in the
        // future we'll get notified and can react accordingly
        Path targetDirectoryPath = copyDefinition.getTargetDirectoryName() == null ? this.getTargetDirectory() : this.getTargetDirectory().resolve(copyDefinition.getTargetDirectoryName());
        GeneratedWebappCopyHandler copyHandler = GeneratedWebappCopyHandler.create(copyDefinition, targetDirectoryPath);

        // Make sure the synchronization stops once the tomcat is stopped as
        // well
        tomcat.getServer().addLifecycleListener(event -> {
            if (Lifecycle.STOP_EVENT.equals(event.getType())) {
                try {
                    copyHandler.close();
                } catch (Exception e) {
                    log.warn("Error occured while closing WatchService", e);
                }
            }
        });

    }

    // -------------------------------------------------------------------------
    // --- Copy definitions ----------------------------------------------------
    // -------------------------------------------------------------------------

    public GeneratedWebappListener addCopyDefinition(Path sourceDirectory) {
        return this.addCopyDefinition(sourceDirectory, null, null, null);
    }

    public GeneratedWebappListener addCopyDefinition(Path sourceDirectory, Predicate<Path> fileFilter) {
        return this.addCopyDefinition(sourceDirectory, null, fileFilter, null);
    }

    public GeneratedWebappListener addCopyDefinition(Path sourceDirectory, String targetDirectoryName) {
        return this.addCopyDefinition(sourceDirectory, targetDirectoryName, null, null);
    }

    public GeneratedWebappListener addCopyDefinition(Path sourceDirectory, String targetDirectoryName, Predicate<Path> fileFilter) {
        return this.addCopyDefinition(sourceDirectory, targetDirectoryName, fileFilter, null);
    }

    public GeneratedWebappListener addCopyDefinition(Path sourceDirectory, String targetDirectoryName, Predicate<Path> fileFilter, List<GeneratedWebappCopyListener> copyListener) {
        GeneratedWebappCopyDefinition copyDefinition = new GeneratedWebappCopyDefinition();
        copyDefinition.setSourceDirectory(sourceDirectory);
        copyDefinition.setTargetDirectoryName(targetDirectoryName);
        copyDefinition.setFileFilter(fileFilter);
        copyDefinition.setCopyListeners(copyListener);
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

    public GeneratedWebappListener targetDirectory(Path targetDirectory) {
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