package jdk.aprismate.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serializer for converting objects to and from byte streams.
 * <p>
 * A Serializer handles the encoding and decoding of objects for
 * persistence, network transmission, or other data interchange needs.
 * </p>
 *
 * @param <T> the type of object this serializer handles
 * @since 26.0-Alpha.8
 */
public interface Serializer<T> {
    
    /**
     * Serializes an object to an output stream.
     * <p>
     * The serializer writes the object's data to the stream in a format
     * that can be later deserialized back to an equivalent object.
     * </p>
     *
     * @param object the object to serialize
     * @param output the output stream to write to
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if object or output is null
     */
    void serialize(T object, OutputStream output) throws IOException;
    
    /**
     * Deserializes an object from an input stream.
     * <p>
     * The serializer reads data from the stream and constructs an object
     * equivalent to the one that was originally serialized.
     * </p>
     *
     * @param input the input stream to read from
     * @return the deserialized object
     * @throws IOException if an I/O error occurs
     * @throws SerializationException if the data is invalid or corrupted
     * @throws NullPointerException if input is null
     */
    T deserialize(InputStream input) throws IOException, SerializationException;
    
    /**
     * Returns the type of object this serializer handles.
     *
     * @return the object type
     */
    Class<T> getType();
    
    /**
     * Returns the format name for this serializer.
     * <p>
     * Common formats include "json", "nbt", "binary", "xml", etc.
     * </p>
     *
     * @return the format name
     */
    default String getFormat() {
        return "binary";
    }
    
    /**
     * Checks if this serializer supports the given type.
     * <p>
     * By default, this checks if the type is assignable from the
     * serializer's declared type.
     * </p>
     *
     * @param type the type to check
     * @return true if the type is supported
     */
    default boolean supports(Class<?> type) {
        return getType().isAssignableFrom(type);
    }
}
