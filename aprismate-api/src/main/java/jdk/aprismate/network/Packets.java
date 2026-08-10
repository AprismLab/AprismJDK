package jdk.aprismate.network;

import java.util.Objects;

/**
 * Static accessor for the global packet registry.
 * <p>
 * This class provides global access to the packet registry used by the runtime.
 * It follows a similar pattern to other static API classes in AprismJDK.
 * </p>
 *
 * @since 26.0-Alpha.7
 */
public final class Packets {
    
    private static volatile PacketRegistry registry;
    
    private Packets() {
        throw new UnsupportedOperationException("Cannot instantiate Packets");
    }
    
    /**
     * Sets the global packet registry.
     * <p>
     * This method should only be called once during initialization by the
     * AprismJDK runtime. Attempting to set the registry multiple times
     * will throw an exception.
     * </p>
     *
     * @param registry the packet registry to use
     * @throws NullPointerException if registry is null
     * @throws IllegalStateException if registry is already set
     */
    public static void setRegistry(PacketRegistry registry) {
        Objects.requireNonNull(registry, "registry cannot be null");
        if (Packets.registry != null) {
            throw new IllegalStateException("Packet registry already initialized");
        }
        Packets.registry = registry;
    }
    
    /**
     * Returns the global packet registry.
     *
     * @return the packet registry
     * @throws IllegalStateException if registry has not been initialized
     */
    public static PacketRegistry getRegistry() {
        PacketRegistry reg = registry;
        if (reg == null) {
            throw new IllegalStateException("Packet registry not initialized");
        }
        return reg;
    }
    
    /**
     * Checks if the packet registry has been initialized.
     *
     * @return true if the registry is set
     */
    public static boolean isInitialized() {
        return registry != null;
    }
    
    /**
     * Clears the packet registry.
     * <p>
     * This is primarily for testing. In production, the registry should
     * remain set for the lifetime of the application.
     * </p>
     */
    public static void clear() {
        registry = null;
    }
}
