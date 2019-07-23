package de.bcersows.photooverlay.controller;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bcersows.photooverlay.model.CalculatedImageSize;
import de.bcersows.photooverlay.model.DragDelta;
import javafx.stage.Stage;

/**
 * The manager to influence the overlay location and size.
 * 
 * @author BCE
 */
public class OverlayLocationManager {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayLocationManager.class);

    /** For the drop-shadow to work correctly we need a bit of extra margin. **/
    private static final double BACKGROUND_DROPSHADOW_MARGIN = 25;

    /** The overlay stage. **/
    private final Stage stage;

    public OverlayLocationManager(@Nonnull final Stage stage) {
        this.stage = stage;
    }

    /**
     * Update the location of the stage using the given drag delta.
     */
    public void updateLocation(@Nonnull final DragDelta dragDelta) {
        LOG.trace("Update location: {}/{}", dragDelta.getDeltaX(), dragDelta.getDeltaY());

        // move the stage
        this.stage.setX(this.stage.getX() + dragDelta.getDeltaX());
        this.stage.setY(this.stage.getY() + dragDelta.getDeltaY());
    }

    /**
     * Resize the stage using the calculated image size.
     */
    public void resize(final CalculatedImageSize calculatedImageSize) {
        this.stage.setWidth(calculatedImageSize.getWidth() + BACKGROUND_DROPSHADOW_MARGIN);
        this.stage.setHeight(calculatedImageSize.getHeight() + BACKGROUND_DROPSHADOW_MARGIN);
    }
}
