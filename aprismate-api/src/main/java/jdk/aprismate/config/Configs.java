package jdk.aprismate.config;

/**
 * Central access point for the global configuration manager.
 * <p>
 * This class provides a static API for accessing configurations throughout
 * the application. The actual ConfigManager implementation is set by
 * the runtime environment.
 * </p>
 *
 * @since 26.0-Alpha.6
 */
public final class Configs {
    
    private static ConfigManager instance;
    
    private Configs() {
        throw new UnsupportedOperationException("Cannot instantiate Configs");
    }
    
    /**
     * Returns the global configuration manager instance.
     *
     * @return the configuration manager
     * @throws IllegalStateException if the configuration manager has not been initialized
     */
    public static ConfigManager getManager() {
        if (instance == null) {
            throw new IllegalStateException("ConfigManager not initialized");
        }
        return instance;
    }
    
    /**
     * Sets the global configuration manager instance.
     * <p>
     * This should only be called by the runtime environment during initialization.
     * </p>
     *
     * @param manager the configuration manager to set
     * @throws NullPointerException if manager is null
     * @throws IllegalStateException if a configuration manager is already set
     */
    public static void setManager(ConfigManager manager) {
        if (manager == null) {
            throw new NullPointerException("manager cannot be null");
        }
        if (instance != null) {
            throw new IllegalStateException("ConfigManager already initialized");
        }
        instance = manager;
    }
    
    /**
     * Checks if the configuration manager has been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return instance != null;
    }
    
    /**
     * Clears the global configuration manager instance.
     * <p>
     * This should only be called during shutdown or for testing.
     * </p>
     */
    public static void clear() {
        instance = null;
    }
}
