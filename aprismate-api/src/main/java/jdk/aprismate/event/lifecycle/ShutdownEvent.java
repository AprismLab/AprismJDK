package jdk.aprismate.event.lifecycle;

import jdk.aprismate.event.CancellableEvent;

/**
 * Fired when the application is about to shut down.
 * <p>
 * This event is fired before the shutdown process begins. Mods can use
 * this event to perform cleanup tasks or to cancel the shutdown if needed.
 * </p>
 * <p>
 * This event is cancellable. If cancelled, the shutdown will be aborted.
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public final class ShutdownEvent extends LifecycleEvent implements CancellableEvent {
    
    private boolean cancelled = false;
    private final ShutdownReason reason;
    
    /**
     * Creates a new shutdown event.
     *
     * @param reason the reason for the shutdown, never {@code null}
     */
    public ShutdownEvent(ShutdownReason reason) {
        super();
        if (reason == null) {
            throw new NullPointerException("reason cannot be null");
        }
        this.reason = reason;
    }
    
    /**
     * Returns the reason for the shutdown.
     *
     * @return the shutdown reason, never {@code null}
     */
    public ShutdownReason getReason() {
        return reason;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    /**
     * Enum representing the reason for a shutdown.
     */
    public enum ShutdownReason {
        /** Normal shutdown requested by the application. */
        NORMAL,
        
        /** Shutdown requested by user (e.g., Ctrl+C). */
        USER_REQUESTED,
        
        /** Shutdown due to an error. */
        ERROR,
        
        /** Shutdown due to system signal (e.g., SIGTERM). */
        SIGNAL
    }
}
