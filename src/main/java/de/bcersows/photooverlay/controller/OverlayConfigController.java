package de.bcersows.photooverlay.controller;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.jfoenix.controls.JFXTextField;

import de.bcersows.photooverlay.ToolConstants;
import de.bcersows.photooverlay.config.OverlayConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * @author BCE
 */
public class OverlayConfigController implements ControllerInterface {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayConfigController.class);

    private static final String TEXT_CURRENTLY_FOUND = "Found %s images.";

    @FXML
    private JFXTextField textFieldFolder;

    @FXML
    private Label labelCurrentlyFound;

    /** The overlay config. **/
    private final OverlayConfig overlayConfig;

    /** The directory chooser to find the image folder. **/
    private final DirectoryChooser directoryChooser;

    /**
     * @param overlayConfig
     */
    @Inject
    protected OverlayConfigController(final OverlayConfig overlayConfig) {
        super();
        this.overlayConfig = overlayConfig;

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("SELECT IMAGE FOLDER");

    }

    @Override
    public void initialize() {

    }

    @Override
    public void show() {
        // TODO fill fields with newest config

        this.textFieldFolder.setText(this.overlayConfig.getFolder());
        updateAmountLabel();
    }

    @FXML
    protected void onActionButtonFolder(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        // set the current folder, if any
        final String currentFolderText = this.textFieldFolder.getText();
        if (StringUtils.isNotBlank(currentFolderText) && new File(currentFolderText).isDirectory()) {
            directoryChooser.setInitialDirectory(new File(currentFolderText));
        }

        // get the current stage and open the dialog
        final Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        final File selectedFolder = directoryChooser.showDialog(stage);
        LOG.info("Selected folder: {}", selectedFolder);

        // if a folder was selected, set it as text
        if (null != selectedFolder) {
            final String chosenPath = selectedFolder.getAbsolutePath();
            this.textFieldFolder.setText(chosenPath);
            this.overlayConfig.setFolder(chosenPath);
            this.overlayConfig.findImages();

            updateAmountLabel();
        }
    }

    /**
     * Update the amount label.
     */
    private void updateAmountLabel() {
        final int amountOfFoundImages = this.overlayConfig.getPhotos().size();
        LOG.info("Found {} images.", amountOfFoundImages);
        this.labelCurrentlyFound.setText(String.format(TEXT_CURRENTLY_FOUND, amountOfFoundImages));
    }

    @FXML
    protected void onActionButtonSave(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        LOG.info("Saved config: {}", this.overlayConfig.saveConfig());
    }

    @FXML
    protected void onActionButtonCancel(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);
    }

}
