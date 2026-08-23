package jdk.aprismate.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * MethodHandle-backed tier.
 *
 * <p>Strategy order per member:
 * <ol>
 *   <li>{@code privateLookupIn} + unreflect (no side effects)</li>
 *   <li>{@code setAccessible(true)} + unreflect on a plain lookup --
 *       valid because unreflect skips access checks when the accessible
 *       flag is set</li>
 * </ol>
 * Failure of both (e.g. JDK-internal members on a locked-down image)
 * propagates to {@link FastReflection} for plain-reflective fallback.
 */
final class MethodHandleAccess {

    private MethodHandleAccess() {
    }

    private static final Object[] NO_ARGS = new Object[0];

    static DirectInvoker invoker(Method m) throws ReflectiveOperationException {
        MethodHandle mh = handle(() -> {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(
                    m.getDeclaringClass(), MethodHandles.lookup());
            return l.unreflect(m);
        }, () -> {
            m.setAccessible(true);
            return MethodHandles.lookup().unreflect(m);
        });
        int arity = m.getParameterCount();
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        if (isStatic) {
            mh = MethodHandles.dropArguments(mh, 0, Object.class);
        }
        MethodHandle spread = mh.asSpreader(Object[].class, arity);
        return new HandleInvoker(m, spread, arity, isStatic);
    }

    static DirectFieldAccessor fieldAccessor(Field f) throws ReflectiveOperationException {
        boolean isStatic = Modifier.isStatic(f.getModifiers());
        MethodHandle getter = handle(() -> {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(
                    f.getDeclaringClass(), MethodHandles.lookup());
            return bind(l.unreflectGetter(f), isStatic);
        }, () -> {
            f.setAccessible(true);
            return bind(MethodHandles.lookup().unreflectGetter(f), isStatic);
        });
        MethodHandle setter = handle(() -> {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(
                    f.getDeclaringClass(), MethodHandles.lookup());
            return bind(l.unreflectSetter(f), isStatic);
        }, () -> {
            f.setAccessible(true);
            return bind(MethodHandles.lookup().unreflectSetter(f), isStatic);
        });
        return new HandleFieldAccessor(f, getter, setter);
    }

    private static MethodHandle bind(MethodHandle h, boolean isStatic) {
        return isStatic ? MethodHandles.dropArguments(h, 0, Object.class) : h;
    }

    @FunctionalInterface
    interface HandleSupplier {
        MethodHandle get() throws ReflectiveOperationException;
    }

    private static MethodHandle handle(HandleSupplier preferred,
                                       HandleSupplier fallback) throws ReflectiveOperationException {
        try {
            return preferred.get();
        } catch (IllegalAccessException | SecurityException e) {
            try {
                return fallback.get();
            } catch (InaccessibleObjectException | SecurityException e2) {
                throw e;
            }
        }
    }

    private record HandleInvoker(Method method, MethodHandle spread, int arity,
                                 boolean isStatic) implements DirectInvoker {

        @Override
        public Object invoke(Object target, Object[] args) throws Throwable {
            Object receiver = isStatic ? null : target;
            Object[] a = args == null ? NO_ARGS : args;
            if (a.length != arity) {
                throw new IllegalArgumentException("expected " + arity + " args, got " + a.length);
            }
            return spread.invoke(receiver, a);
        }

        @Override
        public Strategy strategy() {
            return Strategy.METHOD_HANDLE;
        }
    }

    private record HandleFieldAccessor(Field field, MethodHandle getter,
                                       MethodHandle setter) implements DirectFieldAccessor {

        @Override
        public Object get(Object target) throws Throwable {
            return getter.invoke(target);
        }

        @Override
        public void set(Object target, Object value) throws Throwable {
            setter.invoke(target, value);
        }

        @Override
        public Strategy strategy() {
            return Strategy.METHOD_HANDLE;
        }
    }
}
