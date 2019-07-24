/**
 * 
 */
package de.bcersows.photooverlay;

import javax.annotation.Nonnull;

import com.google.inject.AbstractModule;

import de.bcersows.photooverlay.config.OverlayConfig;
import de.bcersows.photooverlay.controller.OverlayLocationManager;
import javafx.stage.Stage;

/**
 * @author BCE
 */
public class ApplicationConfig extends AbstractModule {
    private final Main main;
    private final OverlayConfig config;
    private final OverlayLocationManager overlayLocationManager;

    /** Initialize the application config. **/
    public ApplicationConfig(@Nonnull final Main main, @Nonnull final OverlayConfig config, @Nonnull final Stage stage) {
        this.main = main;
        this.config = config;
        this.overlayLocationManager = new OverlayLocationManager(stage, config);
    }

    @Override
    protected void configure() {
        bind(Main.class).toInstance(main);
        bind(OverlayConfig.class).toInstance(config);
        bind(OverlayLocationManager.class).toInstance(overlayLocationManager);
    }
}
