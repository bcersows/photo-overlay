package de.bcersows.photooverlay.controller;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.jfoenix.controls.JFXTextField;

import de.bcersows.photooverlay.ToolConstants;
import de.bcersows.photooverlay.config.OverlayConfig;
import de.bcersows.photooverlay.helper.CustomNamedThreadFactory;
import de.bcersows.photooverlay.helper.FxPlatformHelper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * @author BCE
 */
public class OverlayConfigController implements ControllerInterface {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayConfigController.class);

    private static final String TEXT_CURRENTLY_FOUND = "Found %s images in %s.";

    @FXML
    private JFXTextField textFieldFolder;

    @FXML
    private Label labelCurrentlyFound;

    /** The overlay config. **/
    private final OverlayConfig overlayConfig;

    /** The directory chooser to find the image folder. **/
    private final DirectoryChooser directoryChooser;

    /** Executor service. **/
    private final ScheduledExecutorService executor;

    /** Store the config folder to compare against later. **/
    private String configStorageFolder;

    /**
     * @param overlayConfig
     */
    @Inject
    protected OverlayConfigController(@Nonnull final OverlayConfig overlayConfig) {
        super();
        this.overlayConfig = overlayConfig;

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("SELECT IMAGE FOLDER");

        this.executor = Executors.newSingleThreadScheduledExecutor(new CustomNamedThreadFactory("SAVE_TASK"));
    }

    @Override
    public void initialize() {
        // nothing to do yet
    }

    @Override
    public void show() {
        final String configuredFolder = this.overlayConfig.getFolder();
        configStorageFolder = configuredFolder;
        this.textFieldFolder.setText(configuredFolder);
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
        final Stage stage = FxPlatformHelper.getCurrentStage(event);
        final File selectedFolder = directoryChooser.showDialog(stage);
        LOG.info("Selected folder: {}", selectedFolder);

        // if a folder was selected, set it as text
        if (null != selectedFolder) {
            final String chosenPath = selectedFolder.getAbsolutePath();
            this.textFieldFolder.setText(chosenPath);
        }
    }

    /**
     * Update the amount label.
     */
    private void updateAmountLabel() {
        final int amountOfFoundImages = this.overlayConfig.getPhotos().size();
        FxPlatformHelper.runOnFxThread(() -> {
            LOG.trace("Found {} images in {}.", amountOfFoundImages, this.textFieldFolder.getText());
            this.labelCurrentlyFound.setText(String.format(TEXT_CURRENTLY_FOUND, amountOfFoundImages, this.textFieldFolder.getText()));
        });
    }

    @FXML
    protected void onActionButtonSave(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        final Task<Boolean> saveTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // set the folder...
                final String currentFolder = textFieldFolder.getText();
                overlayConfig.setFolder(currentFolder);
                // ... and find the images (if changed)
                if (!StringUtils.equals(configStorageFolder, currentFolder)) {
                    overlayConfig.findImages();
                }

                // save the config
                return overlayConfig.saveConfig();
            }
        };

        saveTask.setOnSucceeded(evt -> {
            LOG.info("Successfully finished saving: {}", saveTask.getValue());
            // close the stage on success
            closeStage(event);
        });
        saveTask.setOnFailed(evt -> LOG.error("Could not save config.", saveTask.getException()));

        // submit the task
        this.executor.submit(saveTask);
    }

    @FXML
    protected void onActionButtonCancel(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        // just close the stage
        closeStage(event);
    }

    /**
     * Close this stage.
     */
    private void closeStage(final ActionEvent event) {
        final Stage stage = FxPlatformHelper.getCurrentStage(event);
        // obviously this call does not trigger the onCloseRequest, so have to do that manually
        stage.close();
        stage.getOnCloseRequest().handle(null);
    }

}
