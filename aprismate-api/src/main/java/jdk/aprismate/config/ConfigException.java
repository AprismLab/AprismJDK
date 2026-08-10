package jdk.aprismate.config;

/**
 * Exception thrown when configuration loading, saving, or parsing fails.
 *
 * @since 26.0-Alpha.6
 */
public class ConfigException extends Exception {
    
    /**
     * Creates a new ConfigException with the given message.
     *
     * @param message the detail message
     */
    public ConfigException(String message) {
        super(message);
    }
    
    /**
     * Creates a new ConfigException with the given message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new ConfigException with the given cause.
     *
     * @param cause the cause of this exception
     */
    public ConfigException(Throwable cause) {
        super(cause);
    }
}
