package jdk.aprismate.agent;

/**
 * Thrown when a class redefinition operation fails.
 * <p>
 * This exception indicates that a structural redefinition operation
 * (redefine class, add/remove field, add method) could not be completed.
 * </p>
 * <p>
 * Common causes include:
 * <ul>
 *   <li>Invalid bytecode</li>
 *   <li>Incompatible structural changes</li>
 *   <li>Class hierarchy violations</li>
 *   <li>VM limitations</li>
 * </ul>
 * </p>
 * 
 * @since 26.1
 */
public class ClassRedefinitionException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new ClassRedefinitionException with the specified detail message.
     */
    public ClassRedefinitionException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new ClassRedefinitionException with the specified detail message and cause.
     */
    public ClassRedefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new ClassRedefinitionException with the specified cause.
     */
    public ClassRedefinitionException(Throwable cause) {
        super(cause);
    }
}
