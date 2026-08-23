package jdk.aprismate.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Correctness baseline used when MethodHandle construction is impossible
 * (e.g. JDK-internal classes on a fully locked-down image). Unwraps
 * {@code InvocationTargetException} so callers see one uniform contract.
 */
final class PlainReflectiveInvoker implements DirectInvoker {

    private final Method method;

    PlainReflectiveInvoker(Method method) {
        this.method = method;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Strategy strategy() {
        return Strategy.PLAIN_REFLECTIVE;
    }
}
