package de.bcersows.photooverlay.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.jfoenix.controls.JFXTextField;

import de.bcersows.photooverlay.ToolConstants;
import de.bcersows.photooverlay.config.OrientationValue;
import de.bcersows.photooverlay.config.OverlayConfig;
import de.bcersows.photooverlay.helper.CustomNamedThreadFactory;
import de.bcersows.photooverlay.helper.FileHelper;
import de.bcersows.photooverlay.helper.FxPlatformHelper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * The controller for the config window.
 * 
 * @author BCE
 */
public class OverlayConfigController implements ControllerInterface {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayConfigController.class);

    /** Text for the amount label. **/
    private static final String TEXT_CURRENTLY_FOUND = "Found %s images in %s.";

    /** The spacing. **/
    private static final double SPACING = 10;

    @FXML
    private JFXTextField textFieldFolder;

    @FXML
    private VBox areaFolders;
    @FXML
    private Button buttonFolderAdd;
    @FXML
    private Label labelCurrentlyFound;

    @FXML
    private RadioButton directionRadioTopLeft;
    @FXML
    private RadioButton directionRadioTopRight;
    @FXML
    private RadioButton directionRadioBottomLeft;
    @FXML
    private RadioButton directionRadioBottomRight;
    @FXML
    private ToggleGroup toggleGroupDirection;

    /** The check box for the cycle setting. **/
    @FXML
    private CheckBox checkBoxCycle;

    /** The overlay config. **/
    private final OverlayConfig overlayConfig;

    /** The directory chooser to find the image folder. **/
    private final DirectoryChooser directoryChooser;

    /** Executor service. **/
    private final ScheduledExecutorService executor;

    /** Store the config folders to compare against later. **/
    @Nonnull
    private final List<String> configStorageFolders = new ArrayList<>();
    /** Store the configured folders' hash at startup. **/
    private int configuredFoldersHashBefore;

    /**
     * Create instance.
     * 
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
        // add the orientation values to the radio button user data
        this.directionRadioTopLeft.setUserData(OrientationValue.TL);
        this.directionRadioTopRight.setUserData(OrientationValue.TR);
        this.directionRadioBottomLeft.setUserData(OrientationValue.BL);
        this.directionRadioBottomRight.setUserData(OrientationValue.BR);

        // if no text was entered, cannot add anything
        this.buttonFolderAdd.disableProperty().bind(this.textFieldFolder.textProperty().isEmpty());
    }

    @Override
    public void prepare() {
        // get the folders and store the hash
        final List<String> configuredFolders = this.overlayConfig.getFolders();
        this.configStorageFolders.clear();
        this.configStorageFolders.addAll(configuredFolders);
        this.configuredFoldersHashBefore = configuredFolders.hashCode();
        this.textFieldFolder.setText(configuredFolders.get(configuredFolders.size() - 1));
        fillFoldersArea(configuredFolders);
        updateAmountLabel();

        // get the orientation and select the respective radio button
        final OrientationValue orientation = this.overlayConfig.getOrientation();
        final RadioButton buttonToSelect;
        switch (orientation) {
            case TR:
                buttonToSelect = this.directionRadioTopRight;
                break;
            case BL:
                buttonToSelect = this.directionRadioBottomLeft;
                break;
            case BR:
                buttonToSelect = this.directionRadioBottomRight;
                break;
            case TL:
                // intentional
            default:
                buttonToSelect = this.directionRadioTopLeft;
                break;
        }
        buttonToSelect.setSelected(true);

        // set other options
        this.checkBoxCycle.setSelected(this.overlayConfig.getCycle());
    }

    @Override
    public void clear() {
        this.configStorageFolders.clear();
    }

    @FXML
    protected void onActionButtonFolderSearch(final ActionEvent event) {
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

    /** Event handler for a click on the "add" button. **/
    @FXML
    protected void onActionButtonFolderAdd(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        final String enteredFolder = this.textFieldFolder.getText();
        // if something was entered...
        if (StringUtils.isNotBlank(enteredFolder)
                // ... it does not exist...
                && !this.configStorageFolders.contains(enteredFolder)
                // ... and the folder actually exists
                && FileHelper.isValidFolder(enteredFolder)) {
            this.configStorageFolders.add(enteredFolder);
            this.textFieldFolder.setText(null);
            fillFoldersArea(this.configStorageFolders);
        } else {
            LOG.trace("Did not add the folder.");
        }
    }

    /** Fill the folders area with rows. **/
    private void fillFoldersArea(final List<String> folders) {
        this.areaFolders.getChildren().clear();

        // for each folder, create a row
        for (final String folder : folders) {
            final Label label = new Label(folder);
            final Button button = new Button(ToolConstants.ICONS.FA_TRASH.code);
            button.getStyleClass().add(ToolConstants.CSS_CLASS_FONT_AWESOME);

            final HBox row = new HBox(SPACING, label, button);
            // directly set an action handler to remove the row
            button.setOnAction(evt -> {
                // first remove the folder from the list and the UI, then...
                this.configStorageFolders.remove(folder);
                this.areaFolders.getChildren().remove(row);
                // ... re-create the area and set the text field text to the removed value
                fillFoldersArea(this.configStorageFolders);
                this.textFieldFolder.setText(folder);
            });

            // add the row
            this.areaFolders.getChildren().add(row);
        }
    }

    /**
     * Update the amount label.
     */
    private void updateAmountLabel() {
        final int amountOfFoundImages = this.overlayConfig.getPhotos().size();
        FxPlatformHelper.runOnFxThread(() -> {
            LOG.trace("Found {} images in {}.", amountOfFoundImages, this.configStorageFolders);
            this.labelCurrentlyFound.setText(String.format(TEXT_CURRENTLY_FOUND, amountOfFoundImages, this.configStorageFolders));
        });
    }

    @FXML
    protected void onActionButtonSave(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        final Task<Boolean> saveTask = new Task<Boolean>() {

            @Override
            protected Boolean call() throws Exception {
                // set the folder...
                overlayConfig.setFolders(configStorageFolders);
                // ... and find the images (if changed)
                if (configuredFoldersHashBefore != configStorageFolders.hashCode()) {
                    overlayConfig.findImages();
                }

                // set the orientation
                final Toggle selectedToggle = toggleGroupDirection.getSelectedToggle();
                if (null != selectedToggle) {
                    final Object orientationUserData = selectedToggle.getUserData();
                    if (null != orientationUserData) {
                        overlayConfig.setOrientation((OrientationValue) orientationUserData);
                    }
                }

                // other options
                overlayConfig.setCycle(checkBoxCycle.isSelected());

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
    private static void closeStage(final ActionEvent event) {
        final Stage stage = FxPlatformHelper.getCurrentStage(event);
        // obviously this call does not trigger the onCloseRequest, so have to do that manually
        stage.close();
        stage.getOnCloseRequest().handle(null);
    }

}
