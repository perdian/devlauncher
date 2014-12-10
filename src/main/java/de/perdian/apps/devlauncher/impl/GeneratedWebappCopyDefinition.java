package de.perdian.apps.devlauncher.impl;

import java.io.File;
import java.io.FileFilter;

/**
 * Defines a location from which the files are copied into the target
 * web application directory
 *
 * @author Christian Robert
 */
public class GeneratedWebappCopyDefinition {

    private File sourceDirectory = null;
    private FileFilter fileFilter = null;
    private String targetDirectoryName = null;

    // ---------------------------------------------------------------------
    // --- Property access methods -----------------------------------------
    // ---------------------------------------------------------------------

    /**
     * Gets the source directory from where the files will be read and
     * copied to the target directory/directories.
     */
    public File getSourceDirectory() {
        return this.sourceDirectory;
    }
    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * Gets the {@code FileFilter} that specifies which of the files within
     * the source directory will be copied
     */
    public FileFilter getFileFilter() {
        return this.fileFilter;
    }
    public void setFileFilter(FileFilter fileFilter) {
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