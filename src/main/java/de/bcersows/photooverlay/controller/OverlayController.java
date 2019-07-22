package de.bcersows.photooverlay.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.bcersows.photooverlay.Main;
import de.bcersows.photooverlay.OverlayConfig;
import de.bcersows.photooverlay.helper.CustomNamedThreadFactory;
import de.bcersows.photooverlay.model.CalculatedImageSize;
import de.bcersows.photooverlay.model.ImageInfo;
import de.bcersows.photooverlay.model.NewLocation;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * @author BCE
 */
public class OverlayController implements ControllerInterface {
    private static final Logger LOG = LoggerFactory.getLogger(OverlayController.class);

    @FXML
    private BorderPane rootContent;

    @FXML
    private Label labelNoImages;

    @FXML
    private VBox imageArea;
    @FXML
    private ImageView imageView;

    // private final OverlayConfig overlayConfig;
    private final OverlayLocationManager overlayLocationManager;
    private final Main main;
    private final OverlayConfig overlayConfig;

    /** Store the latest new location. **/
    @Nonnull
    private NewLocation newLocation = new NewLocation(0, 0, 0, 0, 0, 0);

    /** Executor service. **/
    private final ScheduledExecutorService executor;
    /** If it is currently loading. **/
    private final BooleanProperty loadingInProgress = new SimpleBooleanProperty();

    private Future<?> currentImageLoadTask;

    private String currentUrl;

    @Inject
    public OverlayController(@Nonnull final OverlayLocationManager overlayLocationManager, final Main main, final OverlayConfig overlayConfig) {
        this.overlayLocationManager = overlayLocationManager;
        this.main = main;
        this.overlayConfig = overlayConfig;

        this.executor = Executors.newSingleThreadScheduledExecutor(new CustomNamedThreadFactory("PHOTO_LOAD"));
    }

    @Override
    @FXML
    public void initialize() {
        LOG.info("Init");

        // load the first image
        nextImage();
    }

    /**
     * 
     */
    private final void nextImage() {
        final List<String> possiblePhotos = new ArrayList<>(this.overlayConfig.getPhotos());

        final boolean foundPhotos = !possiblePhotos.isEmpty();
        if (foundPhotos) {

            String url = getNextImageUrl(possiblePhotos);
            while (StringUtils.equals(this.currentUrl, url)) {
                url = getNextImageUrl(possiblePhotos);
            }
            loadImage(url);
        } else {
            LOG.warn("No photos to show!");
        }

        this.labelNoImages.setVisible(!foundPhotos);
        this.labelNoImages.setManaged(!foundPhotos);
        this.imageView.setVisible(foundPhotos);
        this.imageView.setManaged(foundPhotos);
    }

    /**
     * @param possiblePhotos
     * @return
     */
    private String getNextImageUrl(final List<String> possiblePhotos) {
        // FIXME do not create a new random every time
        final int index = new SplittableRandom().nextInt(possiblePhotos.size());
        return possiblePhotos.get(index);
    }

    private void loadImage(@Nonnull final String imageUrl) {
        if (this.loadingInProgress.get() && null != this.currentImageLoadTask) {
            LOG.debug("Cancelled previous task.");
            this.currentImageLoadTask.cancel(true);
        } else {
            this.loadingInProgress.set(true);
        }

        final Task<ImageInfo> imageLoadTask = new Task<ImageInfo>() {
            @Override
            protected ImageInfo call() throws Exception {
                final File f = new File(imageUrl);
                final Image image = new Image(f.toURI().toString());
                return new ImageInfo(imageUrl, image);
            }
        };

        imageLoadTask.setOnCancelled(evt -> {

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
        });
        imageLoadTask.setOnFailed(evt -> {

        });

        this.currentImageLoadTask = this.executor.submit(imageLoadTask);
        LOG.debug("Started loading a new image.");

        this.currentUrl = imageUrl;
    }

    @FXML
    protected void onActionButtonSettings(final ActionEvent event) {
        LOG.debug("Settingsbutton");

        // JFXSnackbar bar = new JFXSnackbar(pane);
        // bar.enqueue(new SnackbarEvent("Notification Msg"))
        this.main.showConfig();
    }

    @FXML
    protected void onActionButtonClose(final ActionEvent event) {
        LOG.debug("Closebutton");

        this.main.onCloseRequest(null);
    }

    @FXML
    protected void onMouseClickedOnImageView(final MouseEvent event) {
        LOG.debug("Mouse clicked");

        nextImage();
    }

    @FXML
    protected void onMouseDraggedEvent(@Nonnull final MouseEvent event) {
        if (isDragButton(event)) {
            LOG.info("Mouse Drag: {}/{}; {}/{}", event.getScreenX(), event.getScreenY(), event.getSceneX(), event.getSceneY());
            this.newLocation = new NewLocation(this.newLocation, event);
            this.overlayLocationManager.updateLocation(newLocation);
        }
    }

    @FXML
    protected void onMousePressedEvent(@Nonnull final MouseEvent event) {
        if (isDragButton(event)) {
            LOG.info("Mouse pressed");
            this.newLocation = new NewLocation(event.getScreenX(), event.getScreenY(), event.getScreenX(), event.getScreenY(), 0, 0);
        }
    }

    /**
     * @return
     */
    private boolean isDragButton(@Nonnull final MouseEvent event) {
        return event.getButton() == MouseButton.PRIMARY;
    }

}
