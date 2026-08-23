package jdk.aprismate.invoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Entry point of the reflection-elimination framework. Acquisition never
 * fails outright: the best achievable tier is returned, degrading to a
 * plain reflective bridge when MethodHandle construction is blocked.
 *
 * <p>All produced accessors are cached per member; hot paths should call
 * once at setup and reuse the instance.
 */
public final class FastReflection {

    private FastReflection() {
    }

    public static DirectInvoker invoker(Method method) {
        validate(method != null, "method");
        try {
            return MethodHandleAccess.invoker(method);
        } catch (ReflectiveOperationException | SecurityException | InaccessibleObjectException e) {
            return new PlainReflectiveInvoker(method);
        }
    }

    public static DirectFieldAccessor fieldAccessor(java.lang.reflect.Field field) {
        validate(field != null, "field");
        try {
            return MethodHandleAccess.fieldAccessor(field);
        } catch (ReflectiveOperationException | SecurityException | InaccessibleObjectException e) {
            return new PlainReflectiveFieldAccessor(field);
        }
    }

    /**
     * Convenience: instantiate via no-arg constructor and immediately
     * invoke the given method on it. Fails with the raw cause chain.
     */
    public static Object createAndInvoke(Class<?> type, Method method, Object[] args) throws Throwable {
        validate(type != null, "type");
        validate(method != null, "method");
        Constructor<?> ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object instance;
        try {
            instance = ctor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        if (!method.getDeclaringClass().isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                    method + " not declared by " + type.getName() + " or supertypes");
        }
        return invoker(method).invoke(instance, args);
    }

    private static void validate(boolean condition, String what) {
        if (!condition) {
            throw new IllegalArgumentException(what + " cannot be null");
        }
    }
}
