/**
 * 
 */
package de.bcersows.photooverlay.model;

import de.bcersows.photooverlay.controller.ControllerInterface;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author BCE
 */
public class LoadedView {
    private final Scene scene;
    private final Parent uiRoot;
    private final ControllerInterface controller;

    private Stage stage;

    /**
     * @param scene
     * @param uiRoot
     * @param controller
     */
    public LoadedView(final Scene scene, final Parent uiRoot, final ControllerInterface controller) {
        super();
        this.scene = scene;
        this.uiRoot = uiRoot;
        this.controller = controller;
    }

    /**
     * @return the scene
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * @return the uiRoot
     */
    public Parent getUiRoot() {
        return this.uiRoot;
    }

    /**
     * @return the controller
     */
    public ControllerInterface getController() {
        return this.controller;
    }

    /**
     * @return the stage
     */
    public Stage getStage() {
        return this.stage;
    }

    /**
     * @param stage
     *            the stage to set
     */
    public void setStage(final Stage stage) {
        this.stage = stage;
    }

}
