package de.bcersows.photooverlay.model;

import de.bcersows.photooverlay.ToolConstants;

/**
 * @author BCE
 */
public class CalculatedImageSize {
    private final double width;

    private final double height;

    /**
     * @param width
     * @param height
     */
    protected CalculatedImageSize(final double width, final double height) {
        super();
        this.width = width;
        this.height = height;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return this.height;
    }

    public static CalculatedImageSize calculate(final double imageWidth, final double imageHeight) {
        final double ratio = imageWidth / imageHeight;

        final double calculatedHeight;
        if (imageWidth > ToolConstants.MAX_IMAGE_SIZE || imageHeight > ToolConstants.MAX_IMAGE_SIZE) {
            if (imageWidth > ToolConstants.MAX_IMAGE_SIZE && imageWidth > imageHeight) {
                calculatedHeight = ToolConstants.MAX_IMAGE_SIZE / ratio;
            } else {
                calculatedHeight = ToolConstants.MAX_IMAGE_SIZE;
            }
        } else {
            calculatedHeight = imageHeight;
        }

        final double calculatedWidth = calculatedHeight * ratio;

        return new CalculatedImageSize(calculatedWidth, calculatedHeight);
    }
}
