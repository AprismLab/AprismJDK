package jdk.aprismate.mod;

/**
 * Represents the environment(s) where a mod can run.
 * <p>
 * Mods can specify whether they are designed for client-side, server-side,
 * or both environments. The mod loader will only load mods that are
 * compatible with the current runtime environment.
 * </p>
 *
 * @since 26.0-Alpha.4
 */
public enum ModEnvironment {
    
    /**
     * The mod runs only on the client side.
     * <p>
     * Client-only mods typically provide UI enhancements, rendering features,
     * or input handling. They will not be loaded on dedicated servers.
     * </p>
     */
    CLIENT,
    
    /**
     * The mod runs only on the server side.
     * <p>
     * Server-only mods typically provide gameplay logic, world generation,
     * or administration features. They will not be loaded on clients.
     * </p>
     */
    SERVER,
    
    /**
     * The mod runs on both client and server.
     * <p>
     * This is the most common environment for mods that provide features
     * needed on both sides, such as new blocks, items, or game mechanics.
     * </p>
     */
    UNIVERSAL;
    
    /**
     * Returns the default mod environment.
     *
     * @return {@link #UNIVERSAL}
     */
    public static ModEnvironment getDefault() {
        return UNIVERSAL;
    }
    
    /**
     * Checks if this environment is compatible with the given environment.
     *
     * @param other the environment to check against
     * @return {@code true} if compatible, {@code false} otherwise
     */
    public boolean isCompatibleWith(ModEnvironment other) {
        if (this == UNIVERSAL || other == UNIVERSAL) {
            return true;
        }
        return this == other;
    }
}
