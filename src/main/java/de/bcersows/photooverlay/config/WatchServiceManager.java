package de.bcersows.photooverlay.config;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.model.FileChanges;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.collections.FXCollections;

/**
 * The manager for the watch service.
 * 
 * @author BCE
 */
public class WatchServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(WatchServiceManager.class);

    /** The folder monitoring service. **/
    private WatchService watchService;
    /** Store the combination of folder path to watch key. **/
    private final ReadOnlyMapWrapper<String, WatchKey> watchKeysPerFolder = new ReadOnlyMapWrapper<>(FXCollections.observableMap(new ConcurrentHashMap<>()));
    /** The thread which will regularly check the folders for any changes. **/
    private Thread watchServiceListener;

    public WatchServiceManager() {
        // nothing
    }

    /**
     * Start the folder monitoring.
     * 
     * @param folderPaths
     *            the paths to monitor
     * @param fileChangesConsumer
     *            the consumer which will be used when a change was detected
     * @throws IOException
     *             if an exception happened
     */
    public synchronized void startFolderMonitoring(@Nonnull final List<String> folderPaths, @Nonnull final Consumer<FileChanges> fileChangesConsumer)
            throws IOException {
        // create the watch service, if it does not exist yet
        if (null == watchService) {
            LOG.debug("Creating new watch service.");
            watchService = FileSystems.getDefault().newWatchService();

            watchServiceListener = new WatchServiceListenerThread(fileChangesConsumer, watchKeysPerFolder.getReadOnlyProperty());
            watchServiceListener.setDaemon(true);
            watchServiceListener.start();
            LOG.debug("Created a new watch service.");
        }

        // start monitoring each folder
        for (final String folder : folderPaths) {
            // if the folder is already being monitored, go to the next one
            if (watchKeysPerFolder.containsKey(folder)) {
                LOG.trace("Already monitoring folder {}.", folder);
                continue;
            }

            final Path folderPath = Paths.get(folder);

            // if the folder does not exist, cannot monitor, continue to next one
            if (!folderPath.toFile().exists()) {
                LOG.debug("Folder {} does not exist, cannot monitor.", folder);
                continue;
            }

            // TODO OVERLAY: also monitor nested folders
            // register the watch key and store it together with the folder name
            final WatchKey watchKey = folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            watchKeysPerFolder.put(folder, watchKey);
            LOG.trace("Starting monitoring folder {}.", folder);
        }

        // need to stop monitoring older folder which are not in use anymore
        // get discrepancy between new list and existing watchers (important to create a new set)
        final Set<String> existingButNotUsedFolders = new HashSet<>(watchKeysPerFolder.keySet());
        /// now only the ones are left which existed, but aren't configured anymore
        existingButNotUsedFolders.removeAll(folderPaths);
        /// cancel the subscription
        existingButNotUsedFolders.forEach(unusedFolder -> watchKeysPerFolder.get(unusedFolder).cancel());
        /// remove the unused from the list
        watchKeysPerFolder.keySet().removeAll(existingButNotUsedFolders);
        LOG.trace("Removed {} old folders: {}.", existingButNotUsedFolders.size(), existingButNotUsedFolders);
    }

    /**
     * Stop the folder monitoring.
     * 
     * @throws IOException
     *             if an exception happened
     */
    public synchronized void stopFolderMonitoring() throws IOException {
        // clean watch service...
        if (null != watchService) {
            watchService.close();
            watchService = null;
            watchKeysPerFolder.clear();
            LOG.debug("Stopped folder monitoring.");
        }
        // ... and listener
        if (null != watchServiceListener) {
            watchServiceListener.interrupt();
            watchServiceListener = null;
        }
    }
}
