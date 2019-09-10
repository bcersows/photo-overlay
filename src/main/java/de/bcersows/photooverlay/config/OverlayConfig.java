package de.bcersows.photooverlay.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.helper.FileHelper;
import de.bcersows.photooverlay.model.FileChangeType;
import de.bcersows.photooverlay.model.FileChanges;

/**
 * Hold the overlay configuration.
 * 
 * @author BCE
 */
public class OverlayConfig {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayConfig.class);

    /** Name of the config file. **/
    private static final String CONFIG_FILE_NAME = "photooverlay.properties";
    /** Separator between multiple values. **/
    private static final String SEPARATOR = ";";

    /** The list of found photos. **/
    private final Set<String> photos = new HashSet<>();

    /** The application config. **/
    private final Properties config = new Properties();
    /** The manager which handles the folder monitoring. **/
    private final WatchServiceManager watchServiceManager;

    public OverlayConfig() {
        this.watchServiceManager = new WatchServiceManager();
    }

    /** Load the config. **/
    public final boolean loadConfig() {
        boolean couldLoad;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CONFIG_FILE_NAME), StandardCharsets.UTF_8)) {
            config.load(reader);

            findImages();
            couldLoad = true;
        } catch (final IOException | InvalidPathException e) {
            LOG.error("Could not load config.", e);
            couldLoad = false;
        }

        return couldLoad;
    }

    /** Save the current config. **/
    public final boolean saveConfig() {
        boolean couldSave;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CONFIG_FILE_NAME), StandardCharsets.UTF_8)) {
            config.store(writer, "CREATED AUTOMATICALLY");

            couldSave = true;
        } catch (final IOException | InvalidPathException e) {
            LOG.error("Could not save config.", e);
            couldSave = false;
        }

        return couldSave;
    }

    /**
     * Find the images of the current folder.
     */
    public void findImages() {
        final List<String> folderPaths = getFolders();

        this.photos.clear();
        if (!folderPaths.isEmpty()) {
            this.photos.addAll(FileHelper.findImages(folderPaths));
            try {
                this.watchServiceManager.startFolderMonitoring(folderPaths, this::monitorFolderChangesConsumer);
            } catch (final IOException e) {
                LOG.error("Could not start folder monitoring.", e);
            }
        } else {
            LOG.warn("No folder(s) given.");
            try {
                this.watchServiceManager.stopFolderMonitoring();
            } catch (final IOException e) {
                LOG.error("Could not stop folder monitoring.", e);
            }
        }
    }

    /**
     * Get the found photos.
     */
    public Set<String> getPhotos() {
        return new HashSet<>(this.photos);
    }

    /**
     * Get the chosen folder(s).
     * 
     * @see OverlayConfigKeys#FOLDER
     **/
    public List<String> getFolders() {
        // get from config...
        final String configuredFolders = this.config.getProperty(OverlayConfigKeys.FOLDER.name(), "");
        // ... and split into list
        return Arrays.asList(configuredFolders.split(SEPARATOR));
    }

    /**
     * Set the folder(s).
     * 
     * @param folders
     */
    public void setFolders(@Nonnull final List<String> folders) {
        // turn list into String...
        final String folderString = folders.stream().collect(Collectors.joining(SEPARATOR));
        // ... and save it
        this.config.put(OverlayConfigKeys.FOLDER.name(), folderString);
    }

    /**
     * Get the overlay orientation.
     * 
     * @see OverlayConfigKeys#ORIENTATION
     **/
    @Nonnull
    public OrientationValue getOrientation() {
        final String property = this.config.getProperty(OverlayConfigKeys.ORIENTATION.name(), OrientationValue.TL.name());

        try {
            return OrientationValue.valueOf(property);
        } catch (final IllegalArgumentException e) {
            LOG.warn("Invalid value for orientation: {}", property);
            return OrientationValue.TL;
        }
    }

    /**
     * Set the overlay orientation.
     * 
     * @param orientation
     */
    public void setOrientation(@Nonnull final OrientationValue orientation) {
        this.config.put(OverlayConfigKeys.ORIENTATION.name(), orientation.name());
    }

    /**
     * Get if to cycle.
     * 
     * @see OverlayConfigKeys#CYCLE
     **/
    @Nonnull
    public boolean isCycle() {
        final String property = this.config.getProperty(OverlayConfigKeys.CYCLE.name(), Boolean.TRUE.toString());
        return Boolean.parseBoolean(property);
    }

    /**
     * Set if to cycle.
     */
    public void setCycle(@Nonnull final boolean cycle) {
        this.config.put(OverlayConfigKeys.CYCLE.name(), Boolean.toString(cycle));
    }

    /**
     * Get if overlay shall be on top.
     * 
     * @see OverlayConfigKeys#ON_TOP
     **/
    @Nonnull
    public boolean isOnTop() {
        final String property = this.config.getProperty(OverlayConfigKeys.ON_TOP.name(), Boolean.TRUE.toString());
        return Boolean.parseBoolean(property);
    }

    /**
     * Set if overlay shall stay on top.
     */
    public void setOnTop(@Nonnull final boolean onTop) {
        this.config.put(OverlayConfigKeys.ON_TOP.name(), Boolean.toString(onTop));
    }

    /** Consumer for the folder monitor. **/
    private void monitorFolderChangesConsumer(@Nonnull final FileChanges fileChanges) {
        fileChanges.getChanges().forEach(change -> {
            LOG.info("Change detected: {}.", change);
            if (change.getType() == FileChangeType.ADDED) {
                this.photos.add(change.getPath());
            } else {
                this.photos.remove(change.getPath());
            }
        });
    }

}
