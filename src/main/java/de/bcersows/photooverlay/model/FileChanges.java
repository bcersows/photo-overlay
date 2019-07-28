package de.bcersows.photooverlay.model;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A set of file changes.
 * 
 * @author BCE
 */
public class FileChanges {
    /** The time of the file changes. **/
    private final Instant time;
    /** The changes themselves. **/
    private final List<FileChange> changes;

    /**
     * Create a new bunch of file changes.
     * 
     * @param changes
     */
    public FileChanges(@Nonnull final List<FileChange> changes) {
        super();
        this.time = Instant.now();
        this.changes = new LinkedList<>(changes);
    }

    /**
     * @return the time
     */
    public Instant getTime() {
        return this.time;
    }

    /**
     * @return the changes
     */
    public List<FileChange> getChanges() {
        return new LinkedList<>(this.changes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("time", time).append("changes", changes).toString();
    }

}
