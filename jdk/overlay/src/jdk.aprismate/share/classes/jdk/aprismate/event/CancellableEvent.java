package jdk.aprismate.event;

/**
 * Interface for events that can be cancelled by listeners.
 * <p>
 * When an event is cancelled, the default behavior associated with the event
 * will not occur. However, cancelling an event does not prevent other listeners
 * from receiving the event.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class PlayerLoginEvent implements CancellableEvent {
 *     private boolean cancelled = false;
 *     private final Player player;
 *     
 *     public PlayerLoginEvent(Player player) {
 *         this.player = player;
 *     }
 *     
 *     @Override
 *     public boolean isCancelled() {
 *         return cancelled;
 *     }
 *     
 *     @Override
 *     public void setCancelled(boolean cancelled) {
 *         this.cancelled = cancelled;
 *     }
 *     
 *     public Player getPlayer() {
 *         return player;
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public interface CancellableEvent extends Event {
    
    /**
     * Returns whether this event has been cancelled.
     *
     * @return {@code true} if the event is cancelled, {@code false} otherwise
     */
    boolean isCancelled();
    
    /**
     * Sets the cancellation state of this event.
     * <p>
     * If set to {@code true}, the default behavior associated with this event
     * will not occur.
     * </p>
     *
     * @param cancelled {@code true} to cancel the event, {@code false} to un-cancel it
     */
    void setCancelled(boolean cancelled);
}
