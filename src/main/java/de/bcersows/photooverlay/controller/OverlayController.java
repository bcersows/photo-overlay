package de.bcersows.photooverlay.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.bcersows.photooverlay.Main;
import de.bcersows.photooverlay.ToolConstants;
import de.bcersows.photooverlay.config.OverlayConfig;
import de.bcersows.photooverlay.helper.CustomNamedThreadFactory;
import de.bcersows.photooverlay.model.CalculatedImageSize;
import de.bcersows.photooverlay.model.DragDelta;
import de.bcersows.photooverlay.model.ImageInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The controller for the photo overlay itself.
 * 
 * @author BCE
 */
public class OverlayController implements ControllerInterface {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayController.class);

    /** The root content. **/
    @FXML
    private StackPane rootContent;

    /** Label to warn of no found images. **/
    @FXML
    private Label labelNoImages;
    /** Label for the text of the drag bar. **/
    @FXML
    private Label dragBarText;

    /** Area containing the image view. **/
    @FXML
    private VBox imageArea;
    /** The image view used to display the photos. **/
    @FXML
    private ImageView imageView;

    /** The overlay location manager. **/
    private final OverlayLocationManager overlayLocationManager;
    /** The main. **/
    private final Main main;
    /** Application config. **/
    private final OverlayConfig overlayConfig;

    /** Store the latest new location. **/
    @Nullable
    private DragDelta newLocation;

    /** Executor service. **/
    private final ScheduledExecutorService executor;
    /** If it is currently loading. **/
    private final BooleanProperty loadingInProgress = new SimpleBooleanProperty();

    /** The current image loading task. **/
    @Nullable
    private Future<?> currentImageLoadTask;
    /** The currently displayed image URL. **/
    private String currentUrl;

    @Inject
    public OverlayController(@Nonnull final OverlayLocationManager overlayLocationManager, final Main main, final OverlayConfig overlayConfig) {
        this.overlayLocationManager = overlayLocationManager;
        this.main = main;
        this.overlayConfig = overlayConfig;

        // start the executor
        this.executor = Executors.newSingleThreadScheduledExecutor(new CustomNamedThreadFactory("PHOTO_LOAD"));
    }

    @Override
    @FXML
    public void initialize() {
        LOG.info("Init overlay.");

        this.dragBarText.setText(ToolConstants.ICONS.FA_DRAG.code);
    }

    @Override
    public void show() {
        // load the first image
        nextImage();
    }

    /**
     * Show the next image.
     */
    private final void nextImage() {
        // 1. get the photos and turn into a list
        final List<String> possiblePhotos = new ArrayList<>(this.overlayConfig.getPhotos());

        // 2. if some exist...
        final boolean foundPhotos = !possiblePhotos.isEmpty();
        if (foundPhotos) {
            /// ... get a random next image URL (but not the current one!)...
            String url = getNextImageUrl(possiblePhotos);
            while (StringUtils.equals(this.currentUrl, url)) {
                url = getNextImageUrl(possiblePhotos);
            }
            /// ... and load it
            loadImage(url);
        } else {
            LOG.warn("No photos to show!");
        }

        // 3. show/hide the labels/image view
        this.labelNoImages.setVisible(!foundPhotos);
        this.labelNoImages.setManaged(!foundPhotos);
        this.imageView.setVisible(foundPhotos);
        this.imageView.setManaged(foundPhotos);
    }

    /**
     * Get the next image URL from the given list, randomly.
     */
    private String getNextImageUrl(@Nonnull final List<String> possiblePhotos) {
        // FIXME do not create a new random every time
        final int index = new SplittableRandom().nextInt(possiblePhotos.size());
        return possiblePhotos.get(index);
    }

    /**
     * Load the image with the given URL.
     */
    private void loadImage(@Nonnull final String imageUrl) {
        // stop the old task, if necessary
        if (this.loadingInProgress.get() && null != this.currentImageLoadTask) {
            LOG.debug("Cancelled previous task.");
            this.currentImageLoadTask.cancel(true);
        } else {
            this.loadingInProgress.set(true);
        }

        // create the actual image load task
        final Task<ImageInfo> imageLoadTask = new Task<ImageInfo>() {
            @Override
            protected ImageInfo call() throws Exception {
                final File f = new File(imageUrl);
                final Image image = new Image(f.toURI().toString());
                return new ImageInfo(imageUrl, image);
            }
        };

        imageLoadTask.setOnCancelled(evt -> {
            LOG.warn("Image loading canceled!");
            this.loadingInProgress.set(false);
        });
        imageLoadTask.setOnSucceeded(evt -> {
            final ImageInfo result = imageLoadTask.getValue();
            imageView.setImage(result.getImage());
            final CalculatedImageSize calculatedImageSize = CalculatedImageSize.calculate(result.getWidth(), result.getHeight());
            imageView.setFitWidth(calculatedImageSize.getWidth());
            imageView.setFitHeight(calculatedImageSize.getHeight());

            rootContent.setMinHeight(calculatedImageSize.getHeight());
            rootContent.setPrefHeight(calculatedImageSize.getHeight());
            rootContent.setMinWidth(calculatedImageSize.getWidth());
            rootContent.setPrefWidth(calculatedImageSize.getWidth());

            this.overlayLocationManager.resize(calculatedImageSize);

            LOG.info("Loaded image {}. Real size: {}/{}, calculated size: {}/{}", result.getUrl(), result.getWidth(), result.getHeight(),
                    calculatedImageSize.getWidth(), calculatedImageSize.getHeight());
            this.loadingInProgress.set(false);
        });
        imageLoadTask.setOnFailed(evt -> {
            LOG.error("Could not load image.", imageLoadTask.getException());
            this.loadingInProgress.set(false);
        });

        // submit the task for execution
        this.currentImageLoadTask = this.executor.submit(imageLoadTask);
        LOG.debug("Started loading a new image.");

        this.currentUrl = imageUrl;
    }

    /** Action event to open the settings. **/
    @FXML
    protected void onActionButtonSettings(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        // JFXSnackbar bar = new JFXSnackbar(pane);
        // bar.enqueue(new SnackbarEvent("Notification Msg"))
        this.main.showConfig();
    }

    /** Action event to close the tool. **/
    @FXML
    protected void onActionButtonClose(final ActionEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        this.main.onCloseRequest(null);
    }

    @FXML
    protected void onMouseClickedOnImageView(final MouseEvent event) {
        LOG.trace(ToolConstants.LOG_TEXT_ACTION, event);

        nextImage();
    }

    /** Event for the pressed mouse drag detection. **/
    @FXML
    protected void onMouseDraggedEvent(@Nonnull final MouseEvent event) {
        if (isDragButton(event)) {
            LOG.trace("Mouse Drag: {}/{}; {}/{}", event.getScreenX(), event.getScreenY(), event.getSceneX(), event.getSceneY());
            this.newLocation = new DragDelta(this.newLocation, event);
            this.overlayLocationManager.updateLocation(newLocation);
        }
    }

    /** Event for the pressed mouse drag start. **/
    @FXML
    protected void onMousePressedEvent(@Nonnull final MouseEvent event) {
        if (isDragButton(event)) {
            LOG.trace("Mouse pressed");
            this.newLocation = new DragDelta(0, 0, event.getScreenX(), event.getScreenY());
        }
    }

    /** Event for the released mouse drag clearance. **/
    @FXML
    protected void onMouseReleasedEvent(@Nonnull final MouseEvent event) {
        if (isDragButton(event)) {
            LOG.trace("Mouse released");
            this.newLocation = null;
        }
    }

    /**
     * Returns if the given mouse event was started using a drag button.
     */
    private boolean isDragButton(@Nonnull final MouseEvent event) {
        return event.getButton() == MouseButton.PRIMARY;
    }

}
