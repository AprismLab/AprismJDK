package jdk.aprismate.serialization;

/**
 * Exception thrown when serialization or deserialization fails.
 * <p>
 * This exception indicates that an error occurred while converting
 * an object to or from a serialized format. Common causes include
 * invalid data, version mismatches, or unsupported types.
 * </p>
 *
 * @since 26.0-Alpha.8
 */
public class SerializationException extends Exception {


    @java.io.Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new serialization exception with no message.
     */
    public SerializationException() {
        super();
    }
    
    /**
     * Constructs a new serialization exception with the given message.
     *
     * @param message the detail message
     */
    public SerializationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new serialization exception with the given cause.
     *
     * @param cause the cause
     */
    public SerializationException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructs a new serialization exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
