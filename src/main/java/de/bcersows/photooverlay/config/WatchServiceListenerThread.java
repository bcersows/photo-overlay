package de.bcersows.photooverlay.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.model.FileChange;
import de.bcersows.photooverlay.model.FileChangeType;
import de.bcersows.photooverlay.model.FileChanges;
import javafx.beans.property.ReadOnlyMapProperty;

/**
 * The thread acting as the watch service listener.
 * 
 * @author BCE
 */
public class WatchServiceListenerThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(WatchServiceListenerThread.class);

    /** How long the watch service listener will sleep between each run. **/
    protected static final long WATCH_SERVICE_LISTENER_INTERVAL = 1000;

    /** The consumer which will be notified of file changes. **/
    private final Consumer<FileChanges> fileChangesConsumer;
    /** The watched keys per folder. **/
    private final ReadOnlyMapProperty<String, WatchKey> watchKeysPerFolder;

    /**
     * Initialize class.
     * 
     * @param fileChangesConsumer
     *            the consumer which will be notified of file changes
     * @param watchKeysPerFolder
     *            the watched keys per folder
     */
    public WatchServiceListenerThread(@Nonnull final Consumer<FileChanges> fileChangesConsumer,
            final ReadOnlyMapProperty<String, WatchKey> watchKeysPerFolder) {
        this.setName("folder-monitoring");

        this.fileChangesConsumer = fileChangesConsumer;
        this.watchKeysPerFolder = watchKeysPerFolder;
    }

    @Override
    public void run() {
        try {
            // thread is supposed to run forever, no need for any breaking condition
            while (true) {
                runWatchServiceListenerInterval(fileChangesConsumer);

                // sleep a bit, that's ok
                Thread.sleep(WATCH_SERVICE_LISTENER_INTERVAL);
            }

        } catch (final InterruptedException e) {
            LOG.warn("Interrupted watch service listener. Will not get events anymore.", e);
        }
    }

    /** A single run of the file changes consumer listener. **/
    private void runWatchServiceListenerInterval(final Consumer<FileChanges> fileChangesConsumer) {
        // create the list for this run
        final List<FileChange> fileChanges = new LinkedList<>();

        // iterate all known watch keys
        final Set<Entry<String, WatchKey>> watchKeyEntries = watchKeysPerFolder.entrySet();
        for (final Entry<String, WatchKey> watchKeyEntry : watchKeyEntries) {
            final WatchKey watchKey = watchKeyEntry.getValue();
            /// for each event of each key, check the type and create a file change
            watchKey.pollEvents().forEach(watchEvent -> {
                final FileChangeType type;
                if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    type = FileChangeType.ADDED;
                } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    type = FileChangeType.REMOVED;
                } else if (watchEvent.kind() == StandardWatchEventKinds.OVERFLOW) {
                    LOG.warn("Overflow happened for path {}. Some events might have been lost.", watchEvent.context());
                    return;
                } else {
                    // should never happen, but does not interest us either
                    return;
                }

                LOG.info("Path: {}/{}", watchKeyEntry.getKey(), watchEvent.context());
                // the context is only the relative path between registered folder and changed file, so need to build a whole path out of it
                final String fileChangePath = Paths.get(
                        // use the base path and a file separator...
                        watchKeyEntry.getKey(), "/",
                        // ... and just the relative file path itself
                        ((Path) watchEvent.context()).toString())
                        // better absolutize :)
                        .toAbsolutePath().toString();
                // create the file change
                fileChanges.add(new FileChange(type, fileChangePath));
            });
        }

        // if there were actual changes, give them to the consumer
        if (!fileChanges.isEmpty()) {
            fileChangesConsumer.accept(new FileChanges(fileChanges));
        }
    }
}
