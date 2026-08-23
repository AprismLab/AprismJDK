package jdk.aprismate.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced reflection utilities for class introspection.
 * Provides simplified access to fields, methods, and constructors.
 */
public interface ClassInfo<T> {
    
    /**
     * Gets the class represented by this ClassInfo.
     *
     * @return the class
     */
    Class<T> getType();
    
    /**
     * Gets the simple name of the class.
     *
     * @return the simple name
     */
    String getName();
    
    /**
     * Gets the fully qualified name of the class.
     *
     * @return the qualified name
     */
    String getQualifiedName();
    
    /**
     * Gets the package name of the class.
     *
     * @return the package name
     */
    String getPackageName();
    
    /**
     * Gets all fields declared in this class.
     *
     * @return list of fields
     */
    List<Field> getFields();
    
    /**
     * Gets all fields including inherited ones.
     *
     * @return list of all fields
     */
    List<Field> getAllFields();
    
    /**
     * Gets a field by name.
     *
     * @param name the field name
     * @return optional containing the field if found
     */
    Optional<Field> getField(String name);
    
    /**
     * Gets all methods declared in this class.
     *
     * @return list of methods
     */
    List<Method> getMethods();
    
    /**
     * Gets all methods including inherited ones.
     *
     * @return list of all methods
     */
    List<Method> getAllMethods();
    
    /**
     * Gets a method by name and parameter types.
     *
     * @param name the method name
     * @param parameterTypes the parameter types
     * @return optional containing the method if found
     */
    Optional<Method> getMethod(String name, Class<?>... parameterTypes);
    
    /**
     * Gets all constructors declared in this class.
     *
     * @return list of constructors
     */
    List<Constructor<T>> getConstructors();
    
    /**
     * Gets a constructor by parameter types.
     *
     * @param parameterTypes the parameter types
     * @return optional containing the constructor if found
     */
    Optional<Constructor<T>> getConstructor(Class<?>... parameterTypes);
    
    /**
     * Creates a new instance using the default constructor.
     *
     * @return new instance
     * @throws ReflectionException if instantiation fails
     */
    T newInstance() throws ReflectionException;
    
    /**
     * Creates a new instance using a constructor with parameters.
     *
     * @param args the constructor arguments
     * @return new instance
     * @throws ReflectionException if instantiation fails
     */
    T newInstance(Object... args) throws ReflectionException;
    
    /**
     * Gets the value of a field.
     *
     * @param instance the instance to get the field from
     * @param fieldName the field name
     * @return the field value
     * @throws ReflectionException if getting the field fails
     */
    Object getFieldValue(T instance, String fieldName) throws ReflectionException;
    
    /**
     * Sets the value of a field.
     *
     * @param instance the instance to set the field on
     * @param fieldName the field name
     * @param value the new value
     * @throws ReflectionException if setting the field fails
     */
    void setFieldValue(T instance, String fieldName, Object value) throws ReflectionException;
    
    /**
     * Invokes a method.
     *
     * @param instance the instance to invoke the method on
     * @param methodName the method name
     * @param args the method arguments
     * @return the method result
     * @throws ReflectionException if invocation fails
     */
    Object invokeMethod(T instance, String methodName, Object... args) throws ReflectionException;
    
    /**
     * Checks if this class is assignable from another class.
     *
     * @param other the other class
     * @return true if assignable
     */
    boolean isAssignableFrom(Class<?> other);
    
    /**
     * Checks if this class is an interface.
     *
     * @return true if interface
     */
    boolean isInterface();
    
    /**
     * Checks if this class is abstract.
     *
     * @return true if abstract
     */
    boolean isAbstract();
    
    /**
     * Checks if this class is an annotation.
     *
     * @return true if annotation
     */
    boolean isAnnotation();
    
    /**
     * Checks if this class is an enum.
     *
     * @return true if enum
     */
    boolean isEnum();
    
    /**
     * Gets the superclass.
     *
     * @return optional containing the superclass if exists
     */
    Optional<Class<?>> getSuperclass();
    
    /**
     * Gets all interfaces implemented by this class.
     *
     * @return list of interfaces
     */
    List<Class<?>> getInterfaces();
}
