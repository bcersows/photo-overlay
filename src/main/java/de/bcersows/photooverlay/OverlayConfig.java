package de.bcersows.photooverlay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        photos.add("D:/Tools/Photo/photos_export/america/_DSC6204.jpg");
        photos.add("D:/Tools/Photo/photos_export/america/_DSC6230.jpg");
        photos.add("D:/Tools/Photo/photos_export/america/_DSC6232.jpg");
        photos.add("D:/Tools/Photo/photos_export/saltyway/IMG_3543.jpg");
        photos.add("D:/Tools/Photo/photos_export/saltyway/_DSC5883.jpg");
    }

    public final boolean loadConfig() {
        boolean couldLoad;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CONFIG_FILE_NAME), StandardCharsets.UTF_8)) {
            config.load(reader);

            couldLoad = true;
        } catch (final IOException | InvalidPathException e) {
            LOG.error("Could not load config.", e);
            couldLoad = false;
        }

        return couldLoad;
    }

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
     * @return the photos
     */
    public Set<String> getPhotos() {
        return new HashSet<>(this.photos);
    }

}
