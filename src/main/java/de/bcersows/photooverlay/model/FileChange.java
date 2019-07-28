package de.bcersows.photooverlay.model;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A single file change.
 * 
 * @author BCE
 */
public class FileChange {
    /** The type of the file. **/
    private final FileChangeType type;
    /** Path of the file. **/
    private final String path;

    /**
     * @param type
     * @param path
     */
    public FileChange(@Nonnull final FileChangeType type, @Nonnull final String path) {
        super();
        this.type = type;
        this.path = path;
    }

    /**
     * @return the type
     */
    public FileChangeType getType() {
        return this.type;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("type", type).append("path", path).toString();
    }

}
