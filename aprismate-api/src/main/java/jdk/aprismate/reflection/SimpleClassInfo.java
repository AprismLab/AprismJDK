package jdk.aprismate.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of ClassInfo.
 */
public class SimpleClassInfo<T> implements ClassInfo<T> {
    
    private final Class<T> type;
    
    public SimpleClassInfo(Class<T> type) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }
    
    @Override
    public Class<T> getType() {
        return type;
    }
    
    @Override
    public String getName() {
        return type.getSimpleName();
    }
    
    @Override
    public String getQualifiedName() {
        return type.getName();
    }
    
    @Override
    public String getPackageName() {
        return type.getPackageName();
    }
    
    @Override
    public List<Field> getFields() {
        return Arrays.asList(type.getDeclaredFields());
    }
    
    @Override
    public List<Field> getAllFields() {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = type;
        
        while (currentClass != null) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }
    
    @Override
    public Optional<Field> getField(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        
        try {
            return Optional.of(type.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            // Try to find in superclasses
            Class<?> currentClass = type.getSuperclass();
            while (currentClass != null) {
                try {
                    return Optional.of(currentClass.getDeclaredField(name));
                } catch (NoSuchFieldException ignored) {
                    currentClass = currentClass.getSuperclass();
                }
            }
            return Optional.empty();
        }
    }
    
    @Override
    public List<Method> getMethods() {
        return Arrays.asList(type.getDeclaredMethods());
    }
    
    @Override
    public List<Method> getAllMethods() {
        List<Method> methods = new ArrayList<>();
        Class<?> currentClass = type;
        
        while (currentClass != null) {
            methods.addAll(Arrays.asList(currentClass.getDeclaredMethods()));
            currentClass = currentClass.getSuperclass();
        }
        
        return methods;
    }
    
    @Override
    public Optional<Method> getMethod(String name, Class<?>... parameterTypes) {
        Objects.requireNonNull(name, "name cannot be null");
        
        try {
            return Optional.of(type.getDeclaredMethod(name, parameterTypes));
        } catch (NoSuchMethodException e) {
            // Try to find in superclasses
            Class<?> currentClass = type.getSuperclass();
            while (currentClass != null) {
                try {
                    return Optional.of(currentClass.getDeclaredMethod(name, parameterTypes));
                } catch (NoSuchMethodException ignored) {
                    currentClass = currentClass.getSuperclass();
                }
            }
            return Optional.empty();
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Constructor<T>> getConstructors() {
        return Arrays.stream(type.getDeclaredConstructors())
                .map(c -> (Constructor<T>) c)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Constructor<T>> getConstructor(Class<?>... parameterTypes) {
        try {
            return Optional.of(type.getDeclaredConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public T newInstance() throws ReflectionException {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new ReflectionException("Failed to create instance of " + type.getName(), e);
        }
    }
    
    @Override
    public T newInstance(Object... args) throws ReflectionException {
        if (args == null || args.length == 0) {
            return newInstance();
        }
        
        Class<?>[] parameterTypes = Arrays.stream(args)
                .map(Object::getClass)
                .toArray(Class<?>[]::new);
        
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException e) {
            // Try to find a compatible constructor
            for (Constructor<T> ctor : getConstructors()) {
                Class<?>[] ctorParams = ctor.getParameterTypes();
                if (ctorParams.length == args.length && isCompatible(ctorParams, parameterTypes)) {
                    try {
                        ctor.setAccessible(true);
                        return ctor.newInstance(args);
                    } catch (Exception ex) {
                        // Continue searching
                    }
                }
            }
            throw new ReflectionException("No compatible constructor found for " + type.getName(), e);
        } catch (Exception e) {
            throw new ReflectionException("Failed to create instance of " + type.getName(), e);
        }
    }
    
    private boolean isCompatible(Class<?>[] ctorParams, Class<?>[] argTypes) {
        for (int i = 0; i < ctorParams.length; i++) {
            if (!ctorParams[i].isAssignableFrom(argTypes[i]) && 
                !isPrimitiveCompatible(ctorParams[i], argTypes[i])) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isPrimitiveCompatible(Class<?> primitive, Class<?> wrapper) {
        if (primitive == int.class) return wrapper == Integer.class;
        if (primitive == long.class) return wrapper == Long.class;
        if (primitive == double.class) return wrapper == Double.class;
        if (primitive == float.class) return wrapper == Float.class;
        if (primitive == boolean.class) return wrapper == Boolean.class;
        if (primitive == byte.class) return wrapper == Byte.class;
        if (primitive == short.class) return wrapper == Short.class;
        if (primitive == char.class) return wrapper == Character.class;
        return false;
    }
    
    @Override
    public Object getFieldValue(T instance, String fieldName) throws ReflectionException {
        Objects.requireNonNull(fieldName, "fieldName cannot be null");
        
        Optional<Field> field = getField(fieldName);
        if (field.isEmpty()) {
            throw new ReflectionException("Field not found: " + fieldName);
        }
        
        try {
            Field f = field.get();
            f.setAccessible(true);
            return f.get(instance);
        } catch (Exception e) {
            throw new ReflectionException("Failed to get field value: " + fieldName, e);
        }
    }
    
    @Override
    public void setFieldValue(T instance, String fieldName, Object value) throws ReflectionException {
        Objects.requireNonNull(fieldName, "fieldName cannot be null");
        
        Optional<Field> field = getField(fieldName);
        if (field.isEmpty()) {
            throw new ReflectionException("Field not found: " + fieldName);
        }
        
        try {
            Field f = field.get();
            f.setAccessible(true);
            f.set(instance, value);
        } catch (Exception e) {
            throw new ReflectionException("Failed to set field value: " + fieldName, e);
        }
    }
    
    @Override
    public Object invokeMethod(T instance, String methodName, Object... args) throws ReflectionException {
        Objects.requireNonNull(methodName, "methodName cannot be null");
        
        Class<?>[] parameterTypes = args == null ? new Class<?>[0] : 
                Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
        
        Optional<Method> method = getMethod(methodName, parameterTypes);
        if (method.isEmpty()) {
            // Try to find a compatible method
            method = findCompatibleMethod(methodName, args);
            if (method.isEmpty()) {
                throw new ReflectionException("Method not found: " + methodName);
            }
        }
        
        try {
            Method m = method.get();
            m.setAccessible(true);
            return m.invoke(instance, args);
        } catch (Exception e) {
            throw new ReflectionException("Failed to invoke method: " + methodName, e);
        }
    }
    
    private Optional<Method> findCompatibleMethod(String name, Object... args) {
        int argCount = args == null ? 0 : args.length;
        
        for (Method method : getAllMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == argCount) {
                if (argCount == 0) {
                    return Optional.of(method);
                }
                
                Class<?>[] paramTypes = method.getParameterTypes();
                Class<?>[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
                
                if (isCompatible(paramTypes, argTypes)) {
                    return Optional.of(method);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean isAssignableFrom(Class<?> other) {
        return type.isAssignableFrom(other);
    }
    
    @Override
    public boolean isInterface() {
        return type.isInterface();
    }
    
    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(type.getModifiers());
    }
    
    @Override
    public boolean isAnnotation() {
        return type.isAnnotation();
    }
    
    @Override
    public boolean isEnum() {
        return type.isEnum();
    }
    
    @Override
    public Optional<Class<?>> getSuperclass() {
        return Optional.ofNullable(type.getSuperclass());
    }
    
    @Override
    public List<Class<?>> getInterfaces() {
        return Arrays.asList(type.getInterfaces());
    }
}
