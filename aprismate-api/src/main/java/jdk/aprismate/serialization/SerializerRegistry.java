package jdk.aprismate.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for serializers.
 * <p>
 * The SerializerRegistry manages the mapping between types and their
 * serializers, and provides methods to serialize and deserialize objects.
 * </p>
 *
 * @since 26.0-Alpha.8
 */
public interface SerializerRegistry {
    
    /**
     * Registers a serializer for a type.
     * <p>
     * If a serializer is already registered for the type, it will be replaced.
     * </p>
     *
     * @param <T> the type
     * @param serializer the serializer to register
     * @throws NullPointerException if serializer is null
     */
    <T> void registerSerializer(Serializer<T> serializer);
    
    /**
     * Registers a serializer for a specific type, overriding the serializer's declared type.
     *
     * @param <T> the type
     * @param type the type to register for
     * @param serializer the serializer to register
     * @throws NullPointerException if type or serializer is null
     */
    <T> void registerSerializer(Class<T> type, Serializer<T> serializer);
    
    /**
     * Returns the serializer for the given type.
     *
     * @param <T> the type
     * @param type the type to get a serializer for
     * @return the serializer, or empty if not registered
     * @throws NullPointerException if type is null
     */
    <T> Optional<Serializer<T>> getSerializer(Class<T> type);
    
    /**
     * Checks if a serializer is registered for the given type.
     *
     * @param type the type to check
     * @return true if a serializer is registered
     * @throws NullPointerException if type is null
     */
    boolean hasSerializer(Class<?> type);
    
    /**
     * Serializes an object to an output stream.
     * <p>
     * This method looks up the appropriate serializer for the object's type
     * and uses it to serialize the object.
     * </p>
     *
     * @param object the object to serialize
     * @param output the output stream to write to
     * @throws IOException if an I/O error occurs
     * @throws SerializationException if no serializer is found or serialization fails
     * @throws NullPointerException if object or output is null
     */
    void serialize(Object object, OutputStream output) throws IOException, SerializationException;
    
    /**
     * Deserializes an object from an input stream.
     * <p>
     * This method uses the serializer for the given type to deserialize
     * the object from the stream.
     * </p>
     *
     * @param <T> the type
     * @param type the type to deserialize to
     * @param input the input stream to read from
     * @return the deserialized object
     * @throws IOException if an I/O error occurs
     * @throws SerializationException if no serializer is found or deserialization fails
     * @throws NullPointerException if type or input is null
     */
    <T> T deserialize(Class<T> type, InputStream input) throws IOException, SerializationException;
    
    /**
     * Returns all registered types.
     *
     * @return set of registered types
     */
    Set<Class<?>> getRegisteredTypes();
    
    /**
     * Unregisters the serializer for the given type.
     *
     * @param type the type to unregister
     * @return true if a serializer was unregistered
     * @throws NullPointerException if type is null
     */
    boolean unregisterSerializer(Class<?> type);
    
    /**
     * Clears all registered serializers.
     * <p>
     * This is primarily for testing. In production, registries should not
     * be cleared after initialization.
     * </p>
     */
    void clear();
}
