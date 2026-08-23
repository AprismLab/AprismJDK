package jdk.aprismate.agent;

import java.lang.reflect.Method;

/**
 * Hook that executes after a method invocation.
 * 
 * <p>Exit hooks receive the method that was invoked, the target object (null for
 * static methods), the return value (null for void methods or if an exception
 * was thrown), and any exception that was thrown.
 * 
 * <p>If an exit hook throws an exception, it is caught and logged, and the
 * hooked method's return value or exception propagates normally. This fail-safe
 * behavior ensures that bad hooks cannot crash the application.
 * 
 * @since v26.1-Alpha.4
 */
@FunctionalInterface
public interface ExitHook {
    
    /**
     * Invoked after the hooked method executes.
     * 
     * <p>This method should execute quickly to minimize performance impact.
     * Long-running operations should be delegated to background threads.
     * 
     * @param method the method that was invoked
     * @param target the target object (null for static methods)
     * @param returnValue the return value (null for void methods or if exception was thrown)
     * @param exception the exception thrown by the method (null if no exception)
     */
    void onExit(Method method, Object target, Object returnValue, Throwable exception);
}
