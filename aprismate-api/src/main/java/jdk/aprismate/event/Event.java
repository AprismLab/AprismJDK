package jdk.aprismate.event;

/**
 * Base interface for all events in the Aprismate event system.
 * <p>
 * Events are immutable data objects that represent something that has happened
 * or is about to happen in the system. Mods can listen to events and respond
 * to them.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyEvent implements Event {
 *     private final String data;
 *     
 *     public MyEvent(String data) {
 *         this.data = data;
 *     }
 *     
 *     public String getData() {
 *         return data;
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public interface Event {
    
    /**
     * Returns whether this event can be cancelled.
     * <p>
     * If an event is cancellable and is cancelled by a listener,
     * the default behavior associated with the event will not occur.
     * </p>
     *
     * @return {@code true} if this event can be cancelled, {@code false} otherwise
     */
    default boolean isCancellable() {
        return this instanceof CancellableEvent;
    }
    
    /**
     * Returns the phase of event dispatch this event is in.
     * <p>
     * Events can be dispatched in multiple phases (e.g., EARLY, DEFAULT, LATE)
     * to allow mods to control the order in which they process events.
     * </p>
     *
     * @return the event phase, never {@code null}
     */
    default EventPhase getPhase() {
        return EventPhase.DEFAULT;
    }
}
