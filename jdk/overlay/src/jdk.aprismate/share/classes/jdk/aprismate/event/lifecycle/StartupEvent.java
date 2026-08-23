package jdk.aprismate.event.lifecycle;

/**
 * Fired when the application is starting up.
 * <p>
 * This event is fired early in the startup process, before most systems
 * are initialized. Mods can use this event to perform early initialization
 * tasks.
 * </p>
 * <p>
 * This event is not cancellable.
 * </p>
 *
 * @since 26.0-Alpha.3
 */
public final class StartupEvent extends LifecycleEvent {
    
    private final String[] args;
    
    /**
     * Creates a new startup event.
     *
     * @param args the command-line arguments passed to the application
     */
    public StartupEvent(String[] args) {
        super();
        this.args = args != null ? args.clone() : new String[0];
    }
    
    /**
     * Returns the command-line arguments passed to the application.
     *
     * @return the command-line arguments, never {@code null}
     */
    public String[] getArgs() {
        return args.clone();
    }
}
