package jdk.aprismate.reflection;

/**
 * Exception thrown when reflection operations fail.
 */
public class ReflectionException extends Exception {


    @java.io.Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new reflection exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ReflectionException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new reflection exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new reflection exception with the specified cause.
     *
     * @param cause the cause
     */
    public ReflectionException(Throwable cause) {
        super(cause);
    }
}
