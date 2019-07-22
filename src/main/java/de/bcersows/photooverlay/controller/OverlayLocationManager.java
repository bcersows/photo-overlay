package de.bcersows.photooverlay.controller;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.model.CalculatedImageSize;
import de.bcersows.photooverlay.model.NewLocation;
import javafx.stage.Stage;

/**
 * @author BCE
 */
public class OverlayLocationManager {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayLocationManager.class);

    private final Stage stage;

    public OverlayLocationManager(@Nonnull final Stage stage) {
        this.stage = stage;
    }

    public void updateLocation(@Nonnull final NewLocation newLocation) {
        LOG.info("Update location: {}/{}", newLocation.getUpdatedX(), newLocation.getUpdatedY());

        this.stage.setX(newLocation.getUpdatedX());
        this.stage.setY(newLocation.getUpdatedY());
    }

    /**
     * @param calculatedImageSize
     */
    public void resize(final CalculatedImageSize calculatedImageSize) {
        this.stage.setWidth(calculatedImageSize.getWidth() + 15);
        this.stage.setHeight(calculatedImageSize.getHeight() + 25);
    }
}