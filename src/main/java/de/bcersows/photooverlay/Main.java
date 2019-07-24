package de.bcersows.photooverlay;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.bcersows.photooverlay.config.OverlayConfig;
import de.bcersows.photooverlay.controller.ControllerInterface;
import de.bcersows.photooverlay.controller.OverlayConfigController;
import de.bcersows.photooverlay.controller.OverlayController;
import de.bcersows.photooverlay.model.LoadedView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * Starts the importer, loads all other activities.
 * 
 * @author BCE
 */
public class Main extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    /** The name of the application. **/
    private static final String APPLICATION_NAME = "Photo Overlay";
    /** The min height of the application. **/
    private static final double WINDOW_MIN_HEIGHT = 600;
    /** The min width of the application. **/
    private static final double WINDOW_MIN_WIDTH = 800;
    /** Path to the CSS file. **/
    private static final String CSS_PATH = "/style/application.css";
    /** Path to the icon file. **/
    private static final String ICON_PATH = "/style/imgs/icon.png";

    private LoadedView overlayView;
    private LoadedView configView;

    @Override
    public void start(@Nonnull final Stage stage) throws Exception {
        LOG.info("Start application.");

        LOG.debug("Initializing.");
        final OverlayConfig overlayConfig = new OverlayConfig();
        overlayConfig.loadConfig();

        stage.setTitle(APPLICATION_NAME);
        stage.setOnCloseRequest(this::onCloseRequest);
        stage.setMinHeight(WINDOW_MIN_HEIGHT);
        stage.setMinWidth(WINDOW_MIN_WIDTH);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream(ICON_PATH)));
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.TRANSPARENT);

        // create the Guice injector
        final Injector injector = Guice.createInjector(new ApplicationConfig(this, overlayConfig, stage));
        this.overlayView = loadView(injector);
        final Scene rootScene = this.overlayView.getScene();
        rootScene.setFill(Color.TRANSPARENT);

        stage.setScene(rootScene);
        stage.show();

        // use the same icon in all windows
        this.configView.getStage().getIcons().add(stage.getIcons().get(0));

        this.overlayView.setStage(stage);
        this.overlayView.getController().prepare();
    }

    /**
     * @param injector
     * @throws IOException
     * @return the loaded overlay view
     */
    private LoadedView loadView(@Nonnull final Injector injector) throws IOException {
        // load the normal photo overlay
        final LoadedView loadedPhotoOverlay = loadActivity(injector, "/fxml/Overlay.fxml", OverlayController.class);

        final LoadedView loadedOverlayConfig = loadActivity(injector, "/fxml/Config.fxml", OverlayConfigController.class);
        final Stage configStage = new Stage();
        configStage.setTitle("CONFIGURATION");
        configStage.setScene(loadedOverlayConfig.getScene());
        configStage.setOnCloseRequest(evt -> showOverlay());
        loadedOverlayConfig.setStage(configStage);
        this.configView = loadedOverlayConfig;

        return loadedPhotoOverlay;
    }

    /**
     * Load the given activity FXML and controller.
     * 
     * @param injector
     * @param fxmlUrl
     * @param controllerClass
     * @return
     * @throws IOException
     */
    private LoadedView loadActivity(@Nonnull final Injector injector, @Nonnull final String fxmlUrl,
            @Nonnull final Class<? extends ControllerInterface> controllerClass) throws IOException {
        final FXMLLoader uiSceneLoader = new FXMLLoader(getClass().getResource(fxmlUrl));
        final ControllerInterface controller = injector.getInstance(controllerClass);
        uiSceneLoader.setController(controller);
        final Parent uiRoot = uiSceneLoader.load();

        final Scene scene = createScene(uiRoot);
        return new LoadedView(scene, uiRoot, controller);
    }

    /** Get the scene or create a new one. **/
    private Scene createScene(@Nonnull final Parent root) {
        final Scene rootScene = new Scene(root);
        rootScene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        Font.loadFont(getClass().getResource("/fonts/fa-solid-900.ttf").toExternalForm(), 36);
        Font.loadFont(getClass().getResource("/fonts/Montserrat-Regular.ttf").toExternalForm(), 36);
        Font.loadFont(getClass().getResource("/fonts/Montserrat-Medium.ttf").toExternalForm(), 36);

        return rootScene;
    }

    /** The close request, shutting down and clearing up everything. **/
    public void onCloseRequest(@Nullable final WindowEvent windowEvent) {
        LOG.info("Shutting down!");

        if (null != windowEvent) {
            windowEvent.consume();
        }

        System.exit(0);
    }

    /** Show the overlay. **/
    public void showOverlay() {
        // first show the overlay...
        this.overlayView.getController().prepare();
        this.overlayView.getStage().show();
        // ... and then clear the config
        this.configView.getController().clear();
        this.configView.getStage().hide();
    }

    /** Show the config. **/
    public void showConfig() {
        // first show the config...
        this.configView.getController().prepare();
        this.configView.getStage().show();
        // ... and then clear the overlay
        this.overlayView.getController().clear();
        this.overlayView.getStage().hide();
    }

    /** Launch the application. **/
    public static void main(final String[] args) {
        Application.launch(args);
    }

}
