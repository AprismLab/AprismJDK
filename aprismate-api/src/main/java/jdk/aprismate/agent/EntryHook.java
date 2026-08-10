package jdk.aprismate.agent;

import java.lang.reflect.Method;

/**
 * Hook that executes before a method invocation.
 * 
 * <p>Entry hooks receive the method being invoked, the target object (null for
 * static methods), and the method arguments. Hooks can inspect but not modify
 * these parameters.
 * 
 * <p>If an entry hook throws an exception, it is caught and logged, and the
 * hooked method continues to execute normally. This fail-safe behavior ensures
 * that bad hooks cannot crash the application.
 * 
 * @since v26.1-Alpha.4
 */
@FunctionalInterface
public interface EntryHook {
    
    /**
     * Invoked before the hooked method executes.
     * 
     * <p>This method should execute quickly to minimize performance impact.
     * Long-running operations should be delegated to background threads.
     * 
     * @param method the method being invoked
     * @param target the target object (null for static methods)
     * @param args the method arguments (empty array if no arguments)
     */
    void onEntry(Method method, Object target, Object[] args);
}
