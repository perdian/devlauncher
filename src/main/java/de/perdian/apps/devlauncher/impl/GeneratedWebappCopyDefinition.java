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