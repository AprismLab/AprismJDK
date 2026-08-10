package jdk.aprismate.event;

/**
 * The central event bus for the Aprismate event system.
 * <p>
 * The event bus is responsible for registering listeners, firing events,
 * and managing the event dispatch lifecycle. All event operations should
 * go through this class.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Register a listener
 * EventBus.register(PlayerLoginEvent.class, event -> {
 *     System.out.println("Player logged in!");
 * });
 * 
 * // Fire an event
 * PlayerLoginEvent event = new PlayerLoginEvent(player);
 * EventBus.fire(event);
 * 
 * // Unregister a listener
 * EventBus.unregister(PlayerLoginEvent.class, listener);
 * }</pre>
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public final class EventBus {
    
    private EventBus() {
        throw new UnsupportedOperationException("EventBus cannot be instantiated");
    }
    
    /**
     * Registers an event listener for the specified event type.
     * <p>
     * The listener will be invoked in the {@link EventPhase#DEFAULT} phase
     * whenever an event of the specified type is fired.
     * </p>
     *
     * @param <T> the type of event to listen for
     * @param eventType the class of the event type, never {@code null}
     * @param listener the listener to register, never {@code null}
     * @throws NullPointerException if eventType or listener is {@code null}
     */
    public static <T extends Event> void register(Class<T> eventType, EventListener<T> listener) {
        register(eventType, listener, EventPhase.DEFAULT);
    }
    
    /**
     * Registers an event listener for the specified event type and phase.
     * <p>
     * The listener will be invoked in the specified phase whenever an event
     * of the specified type is fired.
     * </p>
     *
     * @param <T> the type of event to listen for
     * @param eventType the class of the event type, never {@code null}
     * @param listener the listener to register, never {@code null}
     * @param phase the phase in which the listener should be invoked, never {@code null}
     * @throws NullPointerException if any parameter is {@code null}
     */
    public static <T extends Event> void register(Class<T> eventType, EventListener<T> listener, EventPhase phase) {
        // Implementation will be in aprismate-agent
        throw new UnsupportedOperationException("EventBus.register not yet implemented in Alpha.3");
    }
    
    /**
     * Unregisters an event listener for the specified event type.
     * <p>
     * The listener will no longer be invoked when events of the specified type are fired.
     * If the listener is not registered, this method does nothing.
     * </p>
     *
     * @param <T> the type of event
     * @param eventType the class of the event type, never {@code null}
     * @param listener the listener to unregister, never {@code null}
     * @throws NullPointerException if eventType or listener is {@code null}
     */
    public static <T extends Event> void unregister(Class<T> eventType, EventListener<T> listener) {
        // Implementation will be in aprismate-agent
        throw new UnsupportedOperationException("EventBus.unregister not yet implemented in Alpha.3");
    }
    
    /**
     * Fires an event to all registered listeners.
     * <p>
     * Listeners are invoked in phase order (EARLY, DEFAULT, LATE), and within
     * each phase in the order they were registered.
     * </p>
     * <p>
     * If the event is cancellable and is cancelled by a listener, subsequent
     * listeners will still receive the event, but the method will return {@code false}.
     * </p>
     *
     * @param event the event to fire, never {@code null}
     * @return {@code true} if the event was not cancelled, {@code false} if it was cancelled
     * @throws NullPointerException if event is {@code null}
     */
    public static boolean fire(Event event) {
        // Implementation will be in aprismate-agent
        throw new UnsupportedOperationException("EventBus.fire not yet implemented in Alpha.3");
    }
    
    /**
     * Returns the number of registered listeners for the specified event type.
     *
     * @param eventType the class of the event type, never {@code null}
     * @return the number of registered listeners
     * @throws NullPointerException if eventType is {@code null}
     */
    public static int getListenerCount(Class<? extends Event> eventType) {
        // Implementation will be in aprismate-agent
        throw new UnsupportedOperationException("EventBus.getListenerCount not yet implemented in Alpha.3");
    }
    
    /**
     * Clears all registered listeners.
     * <p>
     * This method is primarily intended for testing purposes.
     * </p>
     */
    public static void clearAll() {
        // Implementation will be in aprismate-agent
        throw new UnsupportedOperationException("EventBus.clearAll not yet implemented in Alpha.3");
    }
}
