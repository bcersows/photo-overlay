package de.bcersows.photooverlay.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.helper.FileHelper;

/**
 * @author BCE
 */
public class OverlayConfig {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayConfig.class);

    /** Name of the config file. **/
    private static final String CONFIG_FILE_NAME = "photooverlay.properties";

    private final Set<String> photos = new HashSet<>();

    /** The application config. **/
    private final Properties config = new Properties();

    public OverlayConfig() {
        // nothing
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
        final String folderPath = getFolder();

        this.photos.clear();
        if (StringUtils.isNotBlank(folderPath)) {
            final File sourceFolder = new File(folderPath);
            this.photos.addAll(FileHelper.findImages(sourceFolder));
        } else {
            LOG.warn("No folder given.");
        }
    }

    /**
     * Get the found photos.
     */
    public Set<String> getPhotos() {
        return new HashSet<>(this.photos);
    }

    /** Get the chosen folder. **/
    public String getFolder() {
        return this.config.getProperty(OverlayConfigKeys.FOLDER.name(), "");
    }

    /**
     * Set the folder.
     * 
     * @param folder
     */
    public void setFolder(final String folder) {
        this.config.put(OverlayConfigKeys.FOLDER.name(), folder);
    }

    /** Get the overlay orientation. **/
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

}
