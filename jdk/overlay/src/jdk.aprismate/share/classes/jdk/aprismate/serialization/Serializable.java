package jdk.aprismate.serialization;

/**
 * Marker interface for objects that can be serialized.
 * <p>
 * Classes implementing this interface indicate they support serialization
 * through the AprismJDK serialization system. Unlike Java's Serializable,
 * this interface is just a marker and does not impose any specific
 * serialization mechanism.
 * </p>
 * <p>
 * The actual serialization behavior is determined by the registered
 * {@link Serializer} for the type.
 * </p>
 *
 * @since 26.0-Alpha.8
 */
public interface Serializable {
    
    /**
     * Returns the serialization version of this object.
     * <p>
     * The version number can be used by serializers to handle different
     * versions of the same class during deserialization. By default,
     * this returns 1.
     * </p>
     *
     * @return the serialization version
     */
    default int getSerializationVersion() {
        return 1;
    }
}
