package de.bcersows.photooverlay.config;

import javax.annotation.Nonnull;

/**
 * The possible orientations.
 * 
 * @author BCE
 */
public enum OrientationValue {
    TL("Top Left"),

    TR("Top Right"),

    BL("Bottom Left"),

    BR("Bottom Right");

    public final String full;

    OrientationValue(@Nonnull final String full) {
        this.full = full;
    }
}
