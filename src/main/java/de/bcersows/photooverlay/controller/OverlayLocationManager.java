package de.bcersows.photooverlay.controller;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.bcersows.photooverlay.config.OrientationValue;
import de.bcersows.photooverlay.config.OverlayConfig;
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
    /** The application config. **/
    private final OverlayConfig config;

    @Inject
    public OverlayLocationManager(@Nonnull final Stage stage, @Nonnull final OverlayConfig config) {
        this.stage = stage;
        this.config = config;
    }

    /**
     * Update the location of the stage using the given drag delta.
     */
    public void updateLocation(@Nonnull final DragDelta dragDelta) {
        LOG.trace("Update location: {}/{}", dragDelta.getDeltaX(), dragDelta.getDeltaY());

        // actually move the stage
        moveStage(dragDelta.getDeltaX(), dragDelta.getDeltaY());
    }

    /**
     * Resize the stage using the calculated image size.
     */
    public void resize(@Nonnull final CalculatedImageSize calculatedImageSize) {
        final double formerWidth = this.stage.getWidth() - BACKGROUND_DROPSHADOW_MARGIN;
        final double formerHeight = this.stage.getHeight() - BACKGROUND_DROPSHADOW_MARGIN;

        // set the new size
        final double newWidth = calculatedImageSize.getWidth();
        final double newHeight = calculatedImageSize.getHeight();
        this.stage.setWidth(newWidth + BACKGROUND_DROPSHADOW_MARGIN);
        this.stage.setHeight(newHeight + BACKGROUND_DROPSHADOW_MARGIN);

        // depending on the configured orientation, might have to move the stage
        final OrientationValue orientation = this.config.getOrientation();
        final double deltaX;
        final double deltaY;
        switch (orientation) {
            case TR:
                deltaX = formerWidth - newWidth;
                deltaY = 0;
                break;
            case BL:
                deltaX = 0;
                deltaY = formerHeight - newHeight;
                break;
            case BR:
                deltaX = formerWidth - newWidth;
                deltaY = formerHeight - newHeight;
                break;
            case TL:
                // intentional
            default:
                deltaX = 0;
                deltaY = 0;
                break;
        }
        moveStage(deltaX, deltaY);
    }

    /** Move the stage with the given delta. **/
    private void moveStage(final double deltaX, final double deltaY) {
        this.stage.setX(this.stage.getX() + Math.floor(deltaX));
        this.stage.setY(this.stage.getY() + Math.floor(deltaY));
    }
}
