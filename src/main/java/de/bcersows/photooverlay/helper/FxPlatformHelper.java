package de.bcersows.photooverlay.helper;

import javax.annotation.Nonnull;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.stage.Stage;

/** A helper for FX platform tasks. **/
public class FxPlatformHelper {
    /** Make sure the provided runnable is always executed on the application thread. **/
    public static void runOnFxThread(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    /**
     * Get the current stage of the given event.
     * 
     * @param event
     *            event to analyze
     * @return the stage the event happened in
     */
    @Nonnull
    public static Stage getCurrentStage(@Nonnull final Event event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}
