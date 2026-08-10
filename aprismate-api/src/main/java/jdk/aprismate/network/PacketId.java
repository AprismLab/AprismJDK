package jdk.aprismate.network;

import java.util.Objects;

/**
 * Unique identifier for a packet type.
 * <p>
 * PacketIds use a namespace:path format similar to ResourceLocation to
 * avoid conflicts between different mods and the base system.
 * </p>
 *
 * @since 26.0-Alpha.7
 */
public final class PacketId implements Comparable<PacketId> {
    
    private static final String DEFAULT_NAMESPACE = "aprism";
    private static final String VALID_PATTERN = "[a-z0-9_.-]+";
    
    private final String namespace;
    private final String path;
    
    /**
     * Creates a new PacketId with the given namespace and path.
     *
     * @param namespace the namespace (e.g., "aprism", "mymod")
     * @param path the path (e.g., "handshake", "chat_message")
     * @throws NullPointerException if namespace or path is null
     * @throws IllegalArgumentException if namespace or path contains invalid characters
     */
    public PacketId(String namespace, String path) {
        if (namespace == null) {
            throw new NullPointerException("namespace cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("path cannot be null");
        }
        if (!namespace.matches(VALID_PATTERN)) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }
        if (!path.matches(VALID_PATTERN)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        
        this.namespace = namespace;
        this.path = path;
    }
    
    /**
     * Creates a PacketId from a string representation.
     * <p>
     * If the string contains a colon, it's split into namespace:path.
     * Otherwise, the default namespace is used.
     * </p>
     *
     * @param id the string representation (e.g., "aprism:handshake" or "handshake")
     * @return the PacketId
     * @throws NullPointerException if id is null
     * @throws IllegalArgumentException if id format is invalid
     */
    public static PacketId of(String id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        
        int colonIndex = id.indexOf(':');
        if (colonIndex < 0) {
            return new PacketId(DEFAULT_NAMESPACE, id);
        }
        
        if (colonIndex == 0 || colonIndex == id.length() - 1) {
            throw new IllegalArgumentException("Invalid packet id format: " + id);
        }
        
        String namespace = id.substring(0, colonIndex);
        String path = id.substring(colonIndex + 1);
        return new PacketId(namespace, path);
    }
    
    /**
     * Returns the namespace of this packet id.
     *
     * @return the namespace, never null
     */
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * Returns the path of this packet id.
     *
     * @return the path, never null
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the default namespace used when none is specified.
     *
     * @return the default namespace
     */
    public static String getDefaultNamespace() {
        return DEFAULT_NAMESPACE;
    }
    
    /**
     * Checks if this packet id uses the default namespace.
     *
     * @return true if using default namespace
     */
    public boolean isDefaultNamespace() {
        return DEFAULT_NAMESPACE.equals(namespace);
    }
    
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PacketId)) return false;
        PacketId other = (PacketId) obj;
        return namespace.equals(other.namespace) && path.equals(other.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
    
    @Override
    public int compareTo(PacketId other) {
        int nsCompare = namespace.compareTo(other.namespace);
        if (nsCompare != 0) return nsCompare;
        return path.compareTo(other.path);
    }
}
