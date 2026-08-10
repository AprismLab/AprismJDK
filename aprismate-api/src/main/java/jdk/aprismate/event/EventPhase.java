package jdk.aprismate.event;

/**
 * Represents the phase of event dispatch.
 * <p>
 * Events can be dispatched in multiple phases, allowing mods to control
 * the order in which they process events. Listeners can register for
 * specific phases to ensure they run before or after other listeners.
 * </p>
 * <p>
 * The standard phases are:
 * <ul>
 *   <li>{@link #EARLY} - Runs before all other phases</li>
 *   <li>{@link #DEFAULT} - The standard phase for most listeners</li>
 *   <li>{@link #LATE} - Runs after all other phases</li>
 * </ul>
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public enum EventPhase {
    
    /**
     * Early phase - runs before all other phases.
     * <p>
     * Use this phase when you need to modify event data before
     * other mods see it, or when you need to set up preconditions.
     * </p>
     */
    EARLY,
    
    /**
     * Default phase - the standard phase for most listeners.
     * <p>
     * Most event listeners should use this phase unless they have
     * a specific reason to run earlier or later.
     * </p>
     */
    DEFAULT,
    
    /**
     * Late phase - runs after all other phases.
     * <p>
     * Use this phase when you need to react to changes made by
     * other mods, or when you need to perform cleanup operations.
     * </p>
     */
    LATE;
    
    /**
     * Returns the default event phase.
     *
     * @return {@link #DEFAULT}
     */
    public static EventPhase getDefault() {
        return DEFAULT;
    }
}
