package de.bcersows.photooverlay.helper;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The image cycle helper contains the logic to cycle images on the overlay.
 * 
 * @author BCE
 */
public class ImageCycleHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ImageCycleHelper.class);

    /** How often (in ms) the cycler will check if necessary to set next image (by decreasing {@link #cycleTimerCounter}). **/
    private static final long IMAGE_CYCLE_CHECK_INTERVAL = 1000;
    /** Every how many check intervals the next image will be shown. **/
    private static final int IMAGE_CYCLE_INTERVAL_NEXT = 5;

    /** The runnable used to show the next image. **/
    @Nonnull
    private final Runnable showNextImageAction;
    /** The eventual consumer which will get called each tick. **/
    @Nullable
    private final Consumer<Integer> updateCounterAction;

    /** A timer to cycle the images. **/
    @Nullable
    private Timer cycleTimer;
    /** The timer task. **/
    @Nullable
    private TimerTask cycleTimerTask;
    /** Have the ability to block the image cycle. **/
    private final AtomicBoolean isImageCycleBlocked = new AtomicBoolean();
    /** The counter to determine if to show the next image. **/
    private final AtomicInteger cycleTimerCounter = new AtomicInteger();

    public ImageCycleHelper(@Nonnull final Runnable showNextImageAction, @Nullable final Consumer<Integer> updateCounterAction) {
        this.showNextImageAction = showNextImageAction;
        this.updateCounterAction = updateCounterAction;
    }

    /** Start the image cycle timer. **/
    public void startImageCycleTimer() {
        stopImageCycleTimer(false);
        this.cycleTimer = new Timer("IMAGE_CYCLE_TIMER", true);
        cycleTimer.scheduleAtFixedRate(getCycleTimerTask(), 0, IMAGE_CYCLE_CHECK_INTERVAL);
    }

    /** Stop the image cycle timer (and the timer, if requested). **/
    public void stopImageCycleTimer(final boolean alsoStopTimer) {
        if (null != this.cycleTimerTask) {
            this.cycleTimerTask.cancel();
            this.cycleTimerTask = null;
        }

        // also stop the timer itself
        if (alsoStopTimer && null != this.cycleTimer) {
            this.cycleTimer.cancel();
            this.cycleTimer.purge();
            this.cycleTimer = null;
        }
    }

    /**
     * Return the cycle timer task, either by using the existing one or by creating a new one.
     */
    public TimerTask getCycleTimerTask() {
        if (null == this.cycleTimerTask) {
            this.cycleTimerTask = new TimerTask() {
                @Override
                public void run() {
                    final int decrementedCounter = cycleTimerCounter.decrementAndGet();
                    final boolean showNext = decrementedCounter <= 0;

                    if (null != updateCounterAction) {
                        updateCounterAction.accept(decrementedCounter);
                    }

                    // only show next if not blocked
                    if (showNext && !isImageCycleBlocked.get()) {
                        LOG.trace("Detected to show next image.");
                        showNextImageAction.run();
                        // also need to reset the interval
                        resetInterval();
                    }
                }
            };
        }

        return this.cycleTimerTask;
    }

    /** Mark the cycle as being blocked. **/
    public void setCycleBlocked(final boolean isBlocked) {
        this.isImageCycleBlocked.set(isBlocked);
    }

    /** Reset the interval counter. **/
    public void resetInterval() {
        LOG.trace("Reset cycle interval.");
        this.cycleTimerCounter.set(IMAGE_CYCLE_INTERVAL_NEXT);
    }
}
