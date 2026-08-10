package jdk.aprismate.event;

/**
 * Functional interface for event listeners.
 * <p>
 * Event listeners are callbacks that are invoked when an event is fired.
 * Listeners can examine the event, modify it (if it's mutable), or cancel
 * it (if it's cancellable).
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * EventListener<PlayerLoginEvent> listener = event -> {
 *     System.out.println("Player logged in: " + event.getPlayer().getName());
 *     
 *     if (event.getPlayer().isBanned()) {
 *         event.setCancelled(true);
 *     }
 * };
 * 
 * EventBus.register(PlayerLoginEvent.class, listener);
 * }</pre>
 * </p>
 *
 * @param <T> the type of event this listener handles
 * @since 26.0-Alpha.3
 */
@FunctionalInterface
public interface EventListener<T extends Event> {
    
    /**
     * Invoked when an event is fired.
     *
     * @param event the event that was fired, never {@code null}
     */
    void onEvent(T event);
}
