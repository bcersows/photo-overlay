package de.bcersows.photooverlay.controller;

import com.google.inject.Inject;

import de.bcersows.photooverlay.OverlayConfig;

/**
 * @author BCE
 */
public class OverlayConfigController implements ControllerInterface {
    private final OverlayConfig overlayConfig;

    /**
     * @param overlayConfig
     */
    @Inject
    protected OverlayConfigController(final OverlayConfig overlayConfig) {
        super();
        this.overlayConfig = overlayConfig;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void show() {
        // TODO fill fields with newest config
    }

}
