package de.bcersows.photooverlay.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javafx.scene.image.Image;

/**
 * @author BCE
 */
public class ImageInfo {
    private final String url;

    private final Image image;

    private final double width;

    private final double height;

    /**
     * @param url
     * @param width
     * @param height
     */
    public ImageInfo(final String url, final Image image) {
        super();
        this.url = url;
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return this.image;
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("url", url).append("width", width).append("height", height).toString();
    }

}
