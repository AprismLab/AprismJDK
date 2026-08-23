package jdk.aprismate.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a single resource that can be read.
 * <p>
 * A Resource provides metadata about its location, source, and contents.
 * Resources are typically obtained from a {@link ResourceManager}.
 * </p>
 *
 * @since 26.0-Alpha.5
 */
public interface Resource extends AutoCloseable {
    
    /**
     * Returns the location of this resource.
     *
     * @return the resource location, never null
     */
    ResourceLocation getLocation();
    
    /**
     * Returns the source of this resource.
     * <p>
     * This is typically the mod ID or "system" for built-in resources.
     * </p>
     *
     * @return the source identifier, never null
     */
    String getSource();
    
    /**
     * Opens an input stream to read this resource.
     * <p>
     * Each call to this method creates a new stream. The caller is
     * responsible for closing the stream.
     * </p>
     *
     * @return an input stream for reading the resource
     * @throws IOException if the resource cannot be opened
     */
    InputStream open() throws IOException;
    
    /**
     * Returns the size of this resource in bytes, if known.
     *
     * @return the size in bytes, or empty if unknown
     */
    default java.util.Optional<Long> getSize() {
        return java.util.Optional.empty();
    }
    
    /**
     * Checks if this resource exists and can be read.
     *
     * @return true if the resource exists and is readable
     */
    default boolean exists() {
        try (InputStream is = open()) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Closes this resource and releases any associated resources.
     * <p>
     * This default implementation does nothing. Implementations that hold
     * resources (file handles, network connections) should override this.
     * </p>
     */
    @Override
    default void close() {
        // Default: no-op
    }
}
