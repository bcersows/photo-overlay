package de.bcersows.photooverlay.controller;

import javafx.fxml.FXML;

/**
 * @author BCE
 */
public interface ControllerInterface {
    /** When showing a controller. **/
    void show();

    /** After initializing. **/
    @FXML
    void initialize();
}
