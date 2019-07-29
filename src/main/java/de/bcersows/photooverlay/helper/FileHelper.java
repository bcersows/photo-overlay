package de.bcersows.photooverlay.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.model.FileChange;
import de.bcersows.photooverlay.model.FileChangeType;
import de.bcersows.photooverlay.model.FileChanges;

/**
 * Helper class for file-related stuff.
 * 
 * @author BCE
 */
public final class FileHelper {
    private static final Logger LOG = LoggerFactory.getLogger(FileHelper.class);

    private static final String FILE_TYPE_GIF = "gif";
    private static final String FILE_TYPE_PNG = "png";
    private static final String FILE_TYPE_JPG = "jpg";
    private static final String[] VALID_FILE_TYPES = new String[] { FILE_TYPE_GIF, FILE_TYPE_JPG, FILE_TYPE_PNG };

    /** How long the watch service listener will sleep between each run. **/
    protected static final long WATCH_SERVICE_LISTENER_INTERVAL = 1000;

    /** The folder monitoring service. **/
    private static WatchService watchService;
    /** Store the combination of folder path to watch key. **/
    private static Map<String, WatchKey> watchKeys = new HashMap<>();
    /** The thread which will regularly check the folders for any changes. **/
    private static Thread watchServiceListener;

    private FileHelper() {
        // nothing
    }

    /** Find all images in the given paths. **/
    public static Set<String> findImages(@Nonnull final List<String> folderPaths) {
        final Set<String> images = new HashSet<>();

        for (final String folderPath : folderPaths) {
            final File folder = new File(folderPath);
            // if the folder exists and is readable, get all files (including from sub-folders)
            if (folder.exists() && folder.canRead()) {
                final IOFileFilter extensionFilter = new SuffixFileFilter(VALID_FILE_TYPES, IOCase.INSENSITIVE);
                final Collection<File> fileList = FileUtils.listFiles(folder, extensionFilter, DirectoryFileFilter.DIRECTORY);
                images.addAll(fileList.stream().parallel().map(File::getAbsolutePath).collect(Collectors.toSet()));
            }
        }

        return images;
    }

    /**
     * Check if the given path is a folder and exists.
     */
    public static boolean isValidFolder(@Nullable final String folderPath) {
        // null values are never valid
        if (null == folderPath) {
            return false;
        }

        final File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }

    /**
     * @param folderPaths
     * @param fileChangesConsumer
     * @throws IOException
     */
    public static synchronized void startFolderMonitoring(@Nonnull final List<String> folderPaths, @Nonnull final Consumer<FileChanges> fileChangesConsumer)
            throws IOException {
        // create the watch service, if it does not exist yet
        if (null == watchService) {
            LOG.debug("Creating new watch service.");
            watchService = FileSystems.getDefault().newWatchService();

            watchServiceListener = new Thread(new Runnable() {

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
                    final Set<Entry<String, WatchKey>> watchKeyEntries = watchKeys.entrySet();
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
            }, "folder-monitoring");
            watchServiceListener.setDaemon(true);
            watchServiceListener.start();
            LOG.debug("Created a new watch service.");
        }

        // start monitoring each folder
        for (final String folder : folderPaths) {
            if (watchKeys.containsKey(folder)) {
                LOG.trace("Already monitoring folder {}.", folder);
                continue;
            }

            final Path folderPath = Paths.get(folder);
            final WatchKey watchKey = folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            watchKeys.put(folder, watchKey);
            LOG.trace("Starting monitoring folder {}.", folder);
        }

        // need to stop monitoring older folder which are not in use anymore
        // get discrepancy between new list and existing watchers (important to create a new set)
        final Set<String> existingButNotUsedFolders = new HashSet<>(watchKeys.keySet());
        /// now only the ones are left which existed, but aren't configured anymore
        existingButNotUsedFolders.removeAll(folderPaths);
        /// cancel the subscription
        existingButNotUsedFolders.forEach(unusedFolder -> watchKeys.get(unusedFolder).cancel());
        /// remove the unused from the list
        watchKeys.keySet().removeAll(existingButNotUsedFolders);
        LOG.trace("Removed {} old folders: {}.", existingButNotUsedFolders.size(), existingButNotUsedFolders);
    }

    /**
     * @throws IOException
     * 
     */
    public static synchronized void stopFolderMonitoring() throws IOException {
        if (null != watchService) {
            watchService.close();
            watchService = null;
            watchKeys.clear();
            LOG.debug("Stopped folder monitoring.");
        }
        if (null != watchServiceListener) {
            watchServiceListener.interrupt();
            watchServiceListener = null;
        }
    }
}
