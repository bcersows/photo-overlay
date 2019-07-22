package de.bcersows.photooverlay.model;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javafx.scene.input.MouseEvent;

/**
 * @author BCE
 */

// https://github.com/goxr3plus/FX-BorderlessScene/blob/master/src/main/java/com/goxr3plus/fxborderlessscene/borderless/BorderlessController.java
public class NewLocation {
    private final double updatedX;
    private final double updatedY;

    private final double inSceneX;
    private final double inSceneY;

    private final double oldX;
    private final double oldY;

    public NewLocation(@Nonnull final NewLocation oldLocation, @Nonnull final MouseEvent event) {
        this(oldLocation, event.getScreenX(), event.getScreenY(), event.getSceneX(), event.getSceneY());
    }

    public NewLocation(@Nonnull final NewLocation oldLocation, final double locationOnScreenX, final double locationOnScreenY, final double locationInSceneX,
            final double locationInSceneY) {
        this(calculateNewX(oldLocation, locationOnScreenX, locationInSceneX), calculateNewY(oldLocation, locationOnScreenY, locationInSceneY),
                oldLocation.updatedX, oldLocation.updatedY, locationInSceneX, locationInSceneY);
    }

    public NewLocation(final double updatedX, final double updatedY, final double oldX, final double oldY, final double inSceneX, final double inSceneY) {
        super();
        this.updatedX = updatedX;
        this.updatedY = updatedY;
        this.oldX = oldX;
        this.oldY = oldY;
        this.inSceneX = inSceneX;
        this.inSceneY = inSceneY;
    }

    /**
     * @param oldLocation
     * @param locationOnScreenX
     * @param locationInSceneX
     * @return
     */
    private static double calculateNewX(@Nonnull final NewLocation oldLocation, final double locationOnScreenX, final double locationInSceneX) {
        return calculateNewValue(oldLocation.oldX, locationOnScreenX, locationInSceneX);
    }

    /**
     * @param oldLocation
     * @param locationOnScreenY
     * @param locationInSceneY
     * @return
     */
    private static double calculateNewY(@Nonnull final NewLocation oldLocation, final double locationOnScreenY, final double locationInSceneY) {
        return calculateNewValue(oldLocation.oldY, locationOnScreenY, locationInSceneY);
    }

    /**
     * @param oldValue
     * @param screenValue
     * @param inSceneValue
     * @return
     */
    private static double calculateNewValue(final double oldValue, final double screenValue, final double inSceneValue) {
        // return screenValue - inSceneValue;
        // return screenValue - (oldValue + screenValue);
        return screenValue;
    }

    /**
     * @return the updatedX
     */
    public double getUpdatedX() {
        return this.updatedX;
    }

    /**
     * @return the updatedY
     */
    public double getUpdatedY() {
        return this.updatedY;
    }

    /**
     * @return the oldX
     */
    public double getOldX() {
        return this.oldX;
    }

    /**
     * @return the oldY
     */
    public double getOldY() {
        return this.oldY;
    }

    /**
     * @return the inSceneX
     */
    public double getInSceneX() {
        return this.inSceneX;
    }

    /**
     * @return the inSceneY
     */
    public double getInSceneY() {
        return this.inSceneY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("updatedX", updatedX).append("updatedY", updatedY).append("oldX", oldX)
                .append("oldY", oldY).toString();
    }

}
