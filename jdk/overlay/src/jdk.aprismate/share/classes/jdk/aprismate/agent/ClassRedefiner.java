package jdk.aprismate.agent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Objects;

/**
 * ClassRedefiner+ provides structural class redefinition capabilities.
 * <p>
 * Unlike standard Java instrumentation which only allows non-structural changes
 * (method body modifications), ClassRedefiner+ enables:
 * <ul>
 *   <li>Adding new fields to existing classes</li>
 *   <li>Removing existing fields</li>
 *   <li>Adding new methods to existing classes</li>
 *   <li>Structural changes with automatic instance migration</li>
 * </ul>
 * </p>
 * <p>
 * This capability requires VM support and is only available on AprismJDK.
 * On stock JDK, operations will throw {@link UnsupportedOperationException}.
 * </p>
 * <p>
 * Thread safety: All methods are thread-safe and can be called concurrently.
 * </p>
 * 
 * @since 26.1
 */
public final class ClassRedefiner {
    
    private static volatile boolean initialized = false;
    private static volatile boolean vmSupportAvailable = false;
    
    // Prevent instantiation
    private ClassRedefiner() {
        throw new AssertionError("ClassRedefiner cannot be instantiated");
    }
    
    /**
     * Checks if ClassRedefiner+ is supported by the current JVM.
     * <p>
     * Returns true only on AprismJDK with VM patches applied.
     * Returns false on stock OpenJDK or older AprismJDK versions.
     * </p>
     */
    public static boolean isSupported() {
        if (!initialized) {
            initialize();
        }
        return vmSupportAvailable;
    }
    
    /**
     * Redefines a class with new bytecode that may include structural changes.
     * <p>
     * Unlike {@link java.lang.instrument.Instrumentation#redefineClasses}, this method
     * supports structural changes such as adding/removing fields and methods.
     * </p>
     * <p>
     * When structural changes are detected, the VM will:
     * <ul>
     *   <li>Create a new class layout</li>
     *   <li>Migrate existing instances to the new layout</li>
     *   <li>Initialize new fields with default values or specified initial values</li>
     *   <li>Update method tables</li>
     * </ul>
     * </p>
     * <p>
     * This operation is atomic: either all changes succeed, or none are applied.
     * </p>
     * 
     * @param clazz the class to redefine
     * @param newClassBytes the new bytecode for the class
     * @throws NullPointerException if clazz or newClassBytes is null
     * @throws UnsupportedOperationException if ClassRedefiner+ is not supported
     * @throws ClassRedefinitionException if the redefinition fails
     */
    public static void redefineClass(Class<?> clazz, byte[] newClassBytes) 
            throws ClassRedefinitionException {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(newClassBytes, "Class bytes cannot be null");
        
        if (!isSupported()) {
            throw new UnsupportedOperationException(
                "ClassRedefiner+ is not supported on this JVM. " +
                "AprismJDK with VM patches is required.");
        }
        
        try {
            redefineClassNative(clazz, newClassBytes);
        } catch (Exception e) {
            throw new ClassRedefinitionException(
                "Failed to redefine class " + clazz.getName(), e);
        }
    }
    
    /**
     * Adds a new field to an existing class.
     * <p>
     * The field is added to the class and all existing instances are migrated
     * to include the new field. The field is initialized with the specified
     * initial value, or with the default value for its type if none specified.
     * </p>
     * <p>
     * Static fields are added to the class and initialized immediately.
     * Instance fields are added to all existing instances with the specified initial value.
     * </p>
     * 
     * @param clazz the class to modify
     * @param field the field descriptor
     * @throws NullPointerException if clazz or field is null
     * @throws UnsupportedOperationException if ClassRedefiner+ is not supported
     * @throws ClassRedefinitionException if the field cannot be added
     */
    public static void addField(Class<?> clazz, FieldDescriptor field) 
            throws ClassRedefinitionException {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(field, "Field descriptor cannot be null");
        
        if (!isSupported()) {
            throw new UnsupportedOperationException(
                "ClassRedefiner+ is not supported on this JVM. " +
                "AprismJDK with VM patches is required.");
        }
        
        try {
            addFieldNative(clazz, field);
        } catch (Exception e) {
            throw new ClassRedefinitionException(
                "Failed to add field " + field.getName() + " to class " + clazz.getName(), e);
        }
    }
    
    /**
     * Removes a field from an existing class.
     * <p>
     * The field is removed from the class and all existing instances are migrated
     * to exclude the field. Any references to the field value are lost.
     * </p>
     * <p>
     * Warning: Removing fields can break code that accesses them via reflection
     * or compiled bytecode. Use with caution.
     * </p>
     * 
     * @param clazz the class to modify
     * @param fieldName the name of the field to remove
     * @throws NullPointerException if clazz or fieldName is null
     * @throws UnsupportedOperationException if ClassRedefiner+ is not supported
     * @throws ClassRedefinitionException if the field cannot be removed
     */
    public static void removeField(Class<?> clazz, String fieldName) 
            throws ClassRedefinitionException {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        
        if (!isSupported()) {
            throw new UnsupportedOperationException(
                "ClassRedefiner+ is not supported on this JVM. " +
                "AprismJDK with VM patches is required.");
        }
        
        try {
            removeFieldNative(clazz, fieldName);
        } catch (Exception e) {
            throw new ClassRedefinitionException(
                "Failed to remove field " + fieldName + " from class " + clazz.getName(), e);
        }
    }
    
    /**
     * Adds a new method to an existing class.
     * <p>
     * The method is added to the class and becomes immediately callable.
     * If bytecode is provided, it is used as the method body. If bytecode is null,
     * the method must be abstract or native.
     * </p>
     * 
     * @param clazz the class to modify
     * @param method the method descriptor
     * @throws NullPointerException if clazz or method is null
     * @throws UnsupportedOperationException if ClassRedefiner+ is not supported
     * @throws ClassRedefinitionException if the method cannot be added
     */
    public static void addMethod(Class<?> clazz, MethodDescriptor method) 
            throws ClassRedefinitionException {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(method, "Method descriptor cannot be null");
        
        // Validate: non-abstract, non-native methods must have bytecode
        if (!method.isAbstract() && !method.isNative() && method.getBytecode() == null) {
            throw new IllegalArgumentException(
                "Method " + method.getName() + " must have bytecode, or be abstract/native");
        }
        
        if (!isSupported()) {
            throw new UnsupportedOperationException(
                "ClassRedefiner+ is not supported on this JVM. " +
                "AprismJDK with VM patches is required.");
        }
        
        try {
            addMethodNative(clazz, method);
        } catch (Exception e) {
            throw new ClassRedefinitionException(
                "Failed to add method " + method.getName() + " to class " + clazz.getName(), e);
        }
    }
    
    /**
     * Pre-validates class bytecode before attempting redefinition.
     * <p>
     * This method checks if the bytecode is structurally valid and compatible
     * with the current class. It does not perform the actual redefinition.
     * </p>
     * <p>
     * Use this to validate bytecode before attempting redefinition to get
     * detailed error messages without affecting the running class.
     * </p>
     * 
     * @param clazz the class to validate against
     * @param newClassBytes the bytecode to validate
     * @return a validation result describing any issues
     * @throws NullPointerException if clazz or newClassBytes is null
     * @throws UnsupportedOperationException if ClassRedefiner+ is not supported
     */
    public static ValidationResult validateBytecode(Class<?> clazz, byte[] newClassBytes) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Objects.requireNonNull(newClassBytes, "Class bytes cannot be null");
        
        if (!isSupported()) {
            throw new UnsupportedOperationException(
                "ClassRedefiner+ is not supported on this JVM. " +
                "AprismJDK with VM patches is required.");
        }
        
        return validateBytecodeNative(clazz, newClassBytes);
    }
    
    // Native methods (stubs for now, will be implemented via VM patches)
    
    private static native void redefineClassNative(Class<?> clazz, byte[] newClassBytes) 
            throws Exception;
    
    private static native void addFieldNative(Class<?> clazz, FieldDescriptor field) 
            throws Exception;
    
    private static native void removeFieldNative(Class<?> clazz, String fieldName) 
            throws Exception;
    
    private static native void addMethodNative(Class<?> clazz, MethodDescriptor method) 
            throws Exception;
    
    private static native ValidationResult validateBytecodeNative(Class<?> clazz, byte[] newClassBytes);
    
    private static native boolean checkVmSupport();
    
    // Initialization
    
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Check if VM support is available
            vmSupportAvailable = checkVmSupport();
        } catch (UnsatisfiedLinkError e) {
            // Native method not found - no VM support
            vmSupportAvailable = false;
            AgentLogger.getInstance().debug(
                "ClassRedefiner+ VM support not available: " + e.getMessage());
        } catch (Exception e) {
            vmSupportAvailable = false;
            AgentLogger.getInstance().warn(
                "Failed to check ClassRedefiner+ VM support", e);
        }
        
        initialized = true;
        
        if (vmSupportAvailable) {
            AgentLogger.getInstance().info("ClassRedefiner+ initialized successfully");
        } else {
            AgentLogger.getInstance().info(
                "ClassRedefiner+ not available (requires AprismJDK with VM patches)");
        }
    }
    
    /**
     * Result of bytecode validation.
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String[] errors;
        
        public ValidationResult(boolean valid, String message, String[] errors) {
            this.valid = valid;
            this.message = message;
            this.errors = errors != null ? errors.clone() : new String[0];
        }
        
        /**
         * Returns true if the bytecode is valid.
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Returns a human-readable validation message.
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Returns detailed error messages, or empty array if valid.
         */
        public String[] getErrors() {
            return errors.clone();
        }
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                   "valid=" + valid +
                   ", message='" + message + '\'' +
                   ", errors=" + java.util.Arrays.toString(errors) +
                   '}';
        }
    }
}
