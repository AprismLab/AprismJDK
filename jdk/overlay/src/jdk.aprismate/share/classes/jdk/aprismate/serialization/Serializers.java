package jdk.aprismate.serialization;

import java.util.Objects;

/**
 * Static accessor for the global serializer registry.
 * <p>
 * This class provides global access to the serializer registry used by the runtime.
 * It follows a similar pattern to other static API classes in AprismJDK.
 * </p>
 *
 * @since 26.0-Alpha.8
 */
public final class Serializers {
    
    private static volatile SerializerRegistry registry;
    
    private Serializers() {
        throw new UnsupportedOperationException("Cannot instantiate Serializers");
    }
    
    /**
     * Sets the global serializer registry.
     * <p>
     * This method should only be called once during initialization by the
     * AprismJDK runtime. Attempting to set the registry multiple times
     * will throw an exception.
     * </p>
     *
     * @param registry the serializer registry to use
     * @throws NullPointerException if registry is null
     * @throws IllegalStateException if registry is already set
     */
    public static void setRegistry(SerializerRegistry registry) {
        Objects.requireNonNull(registry, "registry cannot be null");
        if (Serializers.registry != null) {
            throw new IllegalStateException("Serializer registry already initialized");
        }
        Serializers.registry = registry;
    }
    
    /**
     * Returns the global serializer registry.
     *
     * @return the serializer registry
     * @throws IllegalStateException if registry has not been initialized
     */
    public static SerializerRegistry getRegistry() {
        SerializerRegistry reg = registry;
        if (reg == null) {
            throw new IllegalStateException("Serializer registry not initialized");
        }
        return reg;
    }
    
    /**
     * Checks if the serializer registry has been initialized.
     *
     * @return true if the registry is set
     */
    public static boolean isInitialized() {
        return registry != null;
    }
    
    /**
     * Clears the serializer registry.
     * <p>
     * This is primarily for testing. In production, the registry should
     * remain set for the lifetime of the application.
     * </p>
     */
    public static void clear() {
        registry = null;
    }
}
