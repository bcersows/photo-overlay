package de.bcersows.photooverlay.controller;

import javafx.fxml.FXML;

/**
 * @author BCE
 */
public interface ControllerInterface {
    /** Prepare a controller (be)for(e) showing it. Contrary to {@link #clear()}. **/
    void prepare();

    /** Clear the controller. Contrary to {@link #prepare()}. **/
    void clear();

    /** After initializing. **/
    @FXML
    void initialize();
}
