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
    FOLDER("The image folder. Should be an absolute path.", "."),

    ORIENTATION("Defines which overlay edge will stay the same during resize operations. Possible values: TL,TR, BL, BR", "TL")

    ;

    /** The key description. **/
    private final String description;
    /** The default value. **/
    private final String defaultValue;

    OverlayConfigKeys(@Nonnull final String description, @Nonnull final String defaultValue) {
        this.description = description;
        this.defaultValue = defaultValue;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

}
