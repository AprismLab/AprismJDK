package jdk.aprismate.reflection;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for reflection operations.
 * Provides convenient access to ClassInfo instances.
 */
public final class Reflect {
    
    private static final ConcurrentHashMap<Class<?>, ClassInfo<?>> cache = new ConcurrentHashMap<>();
    
    private Reflect() {
        throw new UnsupportedOperationException("Cannot instantiate Reflect");
    }
    
    /**
     * Gets a ClassInfo for the specified class.
     *
     * @param <T> the class type
     * @param type the class
     * @return the ClassInfo instance
     */
    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> on(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");
        return (ClassInfo<T>) cache.computeIfAbsent(type, SimpleClassInfo::new);
    }
    
    /**
     * Gets a ClassInfo for the class of the specified object.
     *
     * @param <T> the object type
     * @param object the object
     * @return the ClassInfo instance
     */
    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> on(T object) {
        Objects.requireNonNull(object, "object cannot be null");
        return (ClassInfo<T>) on(object.getClass());
    }
    
    /**
     * Gets a ClassInfo for the class with the specified name.
     *
     * @param className the fully qualified class name
     * @return the ClassInfo instance
     * @throws ReflectionException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T> ClassInfo<T> on(String className) throws ReflectionException {
        Objects.requireNonNull(className, "className cannot be null");
        
        try {
            Class<?> type = Class.forName(className);
            return (ClassInfo<T>) on(type);
        } catch (ClassNotFoundException e) {
            throw new ReflectionException("Class not found: " + className, e);
        }
    }
    
    /**
     * Creates a new instance of the specified class.
     *
     * @param <T> the class type
     * @param type the class
     * @return new instance
     * @throws ReflectionException if instantiation fails
     */
    public static <T> T newInstance(Class<T> type) throws ReflectionException {
        return on(type).newInstance();
    }
    
    /**
     * Creates a new instance of the specified class with constructor arguments.
     *
     * @param <T> the class type
     * @param type the class
     * @param args the constructor arguments
     * @return new instance
     * @throws ReflectionException if instantiation fails
     */
    public static <T> T newInstance(Class<T> type, Object... args) throws ReflectionException {
        return on(type).newInstance(args);
    }
    
    /**
     * Gets the value of a field from an object.
     *
     * @param object the object
     * @param fieldName the field name
     * @return the field value
     * @throws ReflectionException if getting the field fails
     */
    @SuppressWarnings("unchecked")
    public static <T> Object getFieldValue(T object, String fieldName) throws ReflectionException {
        Objects.requireNonNull(object, "object cannot be null");
        ClassInfo<T> info = (ClassInfo<T>) on(object.getClass());
        return info.getFieldValue(object, fieldName);
    }
    
    /**
     * Sets the value of a field on an object.
     *
     * @param object the object
     * @param fieldName the field name
     * @param value the new value
     * @throws ReflectionException if setting the field fails
     */
    @SuppressWarnings("unchecked")
    public static <T> void setFieldValue(T object, String fieldName, Object value) throws ReflectionException {
        Objects.requireNonNull(object, "object cannot be null");
        ClassInfo<T> info = (ClassInfo<T>) on(object.getClass());
        info.setFieldValue(object, fieldName, value);
    }
    
    /**
     * Invokes a method on an object.
     *
     * @param object the object
     * @param methodName the method name
     * @param args the method arguments
     * @return the method result
     * @throws ReflectionException if invocation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> Object invokeMethod(T object, String methodName, Object... args) throws ReflectionException {
        Objects.requireNonNull(object, "object cannot be null");
        ClassInfo<T> info = (ClassInfo<T>) on(object.getClass());
        return info.invokeMethod(object, methodName, args);
    }
    
    /**
     * Clears the ClassInfo cache.
     */
    public static void clearCache() {
        cache.clear();
    }
    
    /**
     * Gets the size of the ClassInfo cache.
     *
     * @return cache size
     */
    public static int getCacheSize() {
        return cache.size();
    }
}
