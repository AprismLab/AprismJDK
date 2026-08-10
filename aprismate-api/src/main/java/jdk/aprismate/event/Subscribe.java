package jdk.aprismate.event;

/**
 * Annotation to mark event listener methods.
 * <p>
 * This annotation can be used on methods to automatically register them
 * as event listeners when a mod is loaded. The method must have exactly
 * one parameter of a type that implements {@link Event}.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyMod {
 *     
 *     @Subscribe
 *     public void onPlayerLogin(PlayerLoginEvent event) {
 *         System.out.println("Player logged in!");
 *     }
 *     
 *     @Subscribe(phase = EventPhase.EARLY)
 *     public void onServerStart(ServerStartEvent event) {
 *         // This runs early, before other listeners
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @since 26.0-Alpha.3
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
public @interface Subscribe {
    
    /**
     * The phase in which this listener should be invoked.
     * <p>
     * Defaults to {@link EventPhase#DEFAULT}.
     * </p>
     *
     * @return the event phase
     */
    EventPhase phase() default EventPhase.DEFAULT;
    
    /**
     * Whether this listener should receive cancelled events.
     * <p>
     * If {@code false} (the default), this listener will not be invoked
     * if the event has been cancelled by a previous listener.
     * </p>
     * <p>
     * If {@code true}, this listener will be invoked even if the event
     * has been cancelled.
     * </p>
     *
     * @return {@code true} to receive cancelled events, {@code false} otherwise
     */
    boolean receiveCancelled() default false;
}
