package de.bcersows.photooverlay.helper;

import javafx.application.Platform;

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
}
