package jdk.aprismate.invoke;

import java.lang.reflect.Method;

/**
 * A cached, low-overhead callable bound to one method. Replaces repeated
 * {@code Method.invoke} in hot paths; exceptions thrown by the target
 * propagate <em>unwrapped</em> (never {@code InvocationTargetException}).
 */
public interface DirectInvoker {

    Object invoke(Object target, Object[] args) throws Throwable;

    Method method();

    Strategy strategy();
}
