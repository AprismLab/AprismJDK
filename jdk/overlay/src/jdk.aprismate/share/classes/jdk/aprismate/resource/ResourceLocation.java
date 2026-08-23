package jdk.aprismate.resource;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a namespaced resource identifier.
 * <p>
 * A ResourceLocation consists of a namespace and a path, separated by a colon.
 * For example, {@code minecraft:stone} has namespace "minecraft" and path "stone".
 * </p>
 * <p>
 * This class is immutable and thread-safe.
 * </p>
 *
 * @since 26.0-Alpha.5
 */
public final class ResourceLocation implements Comparable<ResourceLocation> {
    
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_-]+");
    private static final Pattern PATH_PATTERN = Pattern.compile("[a-z0-9_\\-./]+");
    private static final String DEFAULT_NAMESPACE = "aprism";
    
    private final String namespace;
    private final String path;
    
    /**
     * Creates a new resource location with the specified namespace and path.
     *
     * @param namespace the namespace, must match [a-z0-9_-]+
     * @param path the path, must match [a-z0-9_\-./]+
     * @throws IllegalArgumentException if namespace or path are invalid
     * @throws NullPointerException if namespace or path are null
     */
    public ResourceLocation(String namespace, String path) {
        if (namespace == null) {
            throw new NullPointerException("namespace cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("path cannot be null");
        }
        if (!NAMESPACE_PATTERN.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                "Invalid namespace: " + namespace + " (must match [a-z0-9_-]+)");
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException(
                "Invalid path: " + path + " (must match [a-z0-9_\\-./]+)");
        }
        this.namespace = namespace;
        this.path = path;
    }
    
    /**
     * Creates a new resource location by parsing a string.
     * <p>
     * If the string contains a colon, it is split into namespace and path.
     * Otherwise, the default namespace "aprism" is used.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "minecraft:stone"} -> namespace="minecraft", path="stone"</li>
     *   <li>{@code "items/sword"} -> namespace="aprism", path="items/sword"</li>
     * </ul>
     * </p>
     *
     * @param location the string to parse
     * @return the parsed resource location
     * @throws IllegalArgumentException if the location is invalid
     * @throws NullPointerException if location is null
     */
    public static ResourceLocation of(String location) {
        if (location == null) {
            throw new NullPointerException("location cannot be null");
        }
        
        int colonIndex = location.indexOf(':');
        if (colonIndex >= 0) {
            String namespace = location.substring(0, colonIndex);
            String path = location.substring(colonIndex + 1);
            return new ResourceLocation(namespace, path);
        } else {
            return new ResourceLocation(DEFAULT_NAMESPACE, location);
        }
    }
    
    /**
     * Returns the namespace of this resource location.
     *
     * @return the namespace, never null
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * Returns the path of this resource location.
     *
     * @return the path, never null
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the default namespace used when none is specified.
     *
     * @return the default namespace ("aprism")
     */
    public static String getDefaultNamespace() {
        return DEFAULT_NAMESPACE;
    }
    
    /**
     * Returns whether this resource location uses the default namespace.
     *
     * @return true if namespace equals the default namespace
     */
    public boolean isDefaultNamespace() {
        return DEFAULT_NAMESPACE.equals(namespace);
    }
    
    /**
     * Returns the string representation of this resource location.
     * <p>
     * Format: {@code namespace:path}
     * </p>
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResourceLocation)) return false;
        ResourceLocation other = (ResourceLocation) obj;
        return namespace.equals(other.namespace) && path.equals(other.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
    
    @Override
    public int compareTo(ResourceLocation other) {
        int namespaceCompare = this.namespace.compareTo(other.namespace);
        if (namespaceCompare != 0) {
            return namespaceCompare;
        }
        return this.path.compareTo(other.path);
    }
}
