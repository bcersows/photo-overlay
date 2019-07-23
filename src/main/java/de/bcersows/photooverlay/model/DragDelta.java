package de.bcersows.photooverlay.model;

import java.util.function.ToDoubleFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javafx.scene.input.MouseEvent;

/**
 * Depicts a single drag delta.
 * 
 * @author BCE
 */
public class DragDelta {
    /** The delta X. **/
    private final double deltaX;
    /** The delta Y. **/
    private final double deltaY;

    /** The current X location on screen. **/
    private final double locationOnScreenX;
    /** The current Y location on screen. **/
    private final double locationOnScreenY;

    /**
     * Initialize the drag delta.
     * 
     * @param oldLocation
     *            the old location from the last drag event
     * @param event
     *            the drag mouse event
     */
    public DragDelta(@Nullable final DragDelta oldLocation, @Nonnull final MouseEvent event) {
        this(oldLocation, event.getScreenX(), event.getScreenY());
    }

    /**
     * Initialize the drag delta.
     * 
     * @param oldLocation
     *            the old location from the last drag event
     * @param locationOnScreenX
     *            the current value on screen
     * @param locationOnScreenY
     *            the current value on screen
     */
    public DragDelta(@Nullable final DragDelta oldLocation, final double locationOnScreenX, final double locationOnScreenY) {
        this(calculateDeltaX(oldLocation, locationOnScreenX), calculateDeltaY(oldLocation, locationOnScreenY), locationOnScreenX, locationOnScreenY);
    }

    /**
     * Initialize the drag delta.
     * 
     * @param deltaX
     *            delta X
     * @param deltaY
     *            delta Y
     * @param locationOnScreenX
     *            the current value on screen
     * @param locationOnScreenY
     *            the current value on screen
     */
    public DragDelta(final double deltaX, final double deltaY, final double locationOnScreenX, final double locationOnScreenY) {
        super();
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.locationOnScreenX = locationOnScreenX;
        this.locationOnScreenY = locationOnScreenY;
    }

    /**
     * Calculate the delta for X.
     * 
     * @param oldLocation
     *            the old location from the last drag event
     * @param locationOnScreenX
     *            the current value on screen
     * @return
     */
    private static double calculateDeltaX(@Nullable final DragDelta oldLocation, final double locationOnScreenX) {
        return calculateDelta(oldLocation, DragDelta::getLocationOnScreenX, locationOnScreenX);
    }

    /**
     * Calculate the delta for Y.
     * 
     * @param oldLocation
     *            the old location from the last drag event
     * @param locationOnScreenY
     *            the current value on screen
     * @return
     */
    private static double calculateDeltaY(@Nullable final DragDelta oldLocation, final double locationOnScreenY) {
        return calculateDelta(oldLocation, DragDelta::getLocationOnScreenY, locationOnScreenY);
    }

    /**
     * Calculate the delta.
     * 
     * @param oldLocation
     *            the old location from the last drag event
     * @param oldLocationExtractor
     *            function to get the old value
     * @param screenValue
     *            the current value on screen
     * @return
     */
    private static double calculateDelta(@Nullable final DragDelta oldLocation, final ToDoubleFunction<DragDelta> oldLocationExtractor,
            final double screenValue) {
        // try to get the old value from the location, if it is not null
        final double oldValue;
        if (null == oldLocation) {
            // if old location is null, just use the current screen value as well...
            oldValue = screenValue;
        } else {
            // ... else use the extractor function to get it
            oldValue = oldLocationExtractor.applyAsDouble(oldLocation);
        }

        return screenValue - oldValue;
    }

    /**
     * @return the deltaX
     */
    public double getDeltaX() {
        return this.deltaX;
    }

    /**
     * @return the deltaY
     */
    public double getDeltaY() {
        return this.deltaY;
    }

    /**
     * @return the locationOnScreenX
     */
    public double getLocationOnScreenX() {
        return this.locationOnScreenX;
    }

    /**
     * @return the locationOnScreenY
     */
    public double getLocationOnScreenY() {
        return this.locationOnScreenY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("deltaX", deltaX).append("deltaY", deltaY)
                .append("locationOnScreenX", locationOnScreenX).append("locationOnScreenY", locationOnScreenY).toString();
    }

}
