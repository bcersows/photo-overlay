package de.bcersows.photooverlay.model;

import de.bcersows.photooverlay.ToolConstants;

/**
 * Contains data about the size of an image.
 * 
 * @author BCE
 */
public class CalculatedImageSize {
    /** The image width. **/
    private final double width;
    /** The image height. **/
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

    /**
     * Calculate the image size. Scales the largest size down to {@link ToolConstants#MAX_IMAGE_SIZE}.
     * 
     * @param imageWidth
     *            the actual image width
     * @param imageHeight
     *            the actual image height
     * @return the calculated image size
     */
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
