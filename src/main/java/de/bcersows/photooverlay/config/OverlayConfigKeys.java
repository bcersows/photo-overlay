/**
 * 
 */
package de.bcersows.photooverlay.config;

import javax.annotation.Nonnull;

/**
 * The possible overlay value keys.
 * 
 * @author BCE
 */
public enum OverlayConfigKeys {
    FOLDER("The image folder. Should be an absolute path."),

    ORIENTATION("")

    ;

    private final String description;

    private OverlayConfigKeys(@Nonnull final String description) {
        this.description = description;
    }
}
