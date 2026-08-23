package jdk.aprismate.event.lifecycle;

import jdk.aprismate.event.Event;

/**
 * Base class for lifecycle events.
 * <p>
 * Lifecycle events represent major phases in the application lifecycle,
 * such as startup, shutdown, or phase transitions.
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public abstract class LifecycleEvent implements Event {
    
    private final long timestamp;
    
    protected LifecycleEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Returns the timestamp when this event was created.
     *
     * @return the event timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
}
