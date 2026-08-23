package jdk.aprismate.resource;

/**
 * Central access point for the global resource manager.
 * <p>
 * This class provides a static API for accessing resources throughout
 * the application. The actual ResourceManager implementation is set by
 * the runtime environment.
 * </p>
 *
 * @since 26.0-Alpha.5
 */
public final class Resources {
    
    private static ResourceManager instance;
    
    private Resources() {
        throw new UnsupportedOperationException("Cannot instantiate Resources");
    }
    
    /**
     * Returns the global resource manager instance.
     *
     * @return the resource manager
     * @throws IllegalStateException if the resource manager has not been initialized
     */
    public static ResourceManager getManager() {
        if (instance == null) {
            throw new IllegalStateException("ResourceManager not initialized");
        }
        return instance;
    }
    
    /**
     * Sets the global resource manager instance.
     * <p>
     * This should only be called by the runtime environment during initialization.
     * </p>
     *
     * @param manager the resource manager to set
     * @throws NullPointerException if manager is null
     * @throws IllegalStateException if a resource manager is already set
     */
    public static void setManager(ResourceManager manager) {
        if (manager == null) {
            throw new NullPointerException("manager cannot be null");
        }
        if (instance != null) {
            throw new IllegalStateException("ResourceManager already initialized");
        }
        instance = manager;
    }
    
    /**
     * Checks if the resource manager has been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return instance != null;
    }
    
    /**
     * Clears the global resource manager instance.
     * <p>
     * This should only be called during shutdown or for testing.
     * </p>
     */
    public static void clear() {
        instance = null;
    }
}
