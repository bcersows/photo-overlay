package de.bcersows.photooverlay.helper;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

/**
 * Can be used to give custom names to {@link java.util.concurrent.ExecutorService}s.<br/>
 * Usage:
 * 
 * <pre>
 * ScheduledExecutorService ses = Executors.newScheduledThreadPool(1, new CustomNamedThreadFactory("name"));
 * </pre>
 * 
 * @author BCE
 *
 */
public class CustomNamedThreadFactory implements ThreadFactory {
    /** Thread name. **/
    @Nonnull
    private final String threadName;
    /** Counter. **/
    private final AtomicInteger cnt = new AtomicInteger(0);
    /** If a counter shall be appended. **/
    private final boolean addCounter;

    /**
     * Create a thread factory for a custom name. The given thread name is a prefix, a counter will be appended.
     * 
     * @param threadName
     *            specified thread name prefix
     */
    public CustomNamedThreadFactory(@Nonnull final String threadName) {
        this(threadName, true);
    }

    /**
     * Create a thread factory for a custom name.
     * 
     * @param threadName
     *            specified thread name prefix
     * @param addCounter
     *            if a counter shall be appended or if all threads shall have the same name
     */
    public CustomNamedThreadFactory(@Nonnull final String threadName, final boolean addCounter) {
        this.threadName = threadName;
        this.addCounter = addCounter;
    }

    @Override
    public Thread newThread(final Runnable r) {
        if (addCounter) {
            return new Thread(r, threadName + "-" + cnt.getAndIncrement());
        } else {
            return new Thread(r, threadName);
        }
    }

}
