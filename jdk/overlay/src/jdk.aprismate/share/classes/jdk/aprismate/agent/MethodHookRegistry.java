package jdk.aprismate.agent;

import java.lang.reflect.Method;

/**
 * Registry for method entry and exit hooks.
 * 
 * <p>MethodHookRegistry+ allows registering hooks that execute before and after
 * method invocations. These hooks survive JIT compilation by treating hook points
 * as deoptimization anchors.
 * 
 * <p>This is an AprismJDK-specific capability. On stock JDKs, use ASM method
 * wrapping or standard Java agent transformation instead.
 * 
 * @since v26.1-Alpha.4
 */
public interface MethodHookRegistry {
    
    /**
     * Registers an entry hook for the specified method.
     * 
     * <p>The hook will be invoked before each execution of the method, including
     * JIT-compiled invocations. If the method is already JIT-compiled, it will
     * be deoptimized to ensure the hook is executed.
     * 
     * @param method the method to hook
     * @param hook the hook to execute before method entry
     * @throws IllegalArgumentException if method is null or hook is null
     * @throws UnsupportedOperationException if the method cannot be hooked
     */
    void registerEntryHook(Method method, EntryHook hook);
    
    /**
     * Registers an exit hook for the specified method.
     * 
     * <p>The hook will be invoked after each execution of the method, including
     * JIT-compiled invocations. The hook receives the return value (or null for
     * void methods) and any thrown exception.
     * 
     * @param method the method to hook
     * @param hook the hook to execute after method exit
     * @throws IllegalArgumentException if method is null or hook is null
     * @throws UnsupportedOperationException if the method cannot be hooked
     */
    void registerExitHook(Method method, ExitHook hook);
    
    /**
     * Unregisters all hooks for the specified method.
     * 
     * @param method the method to unhook
     * @return true if hooks were removed, false if no hooks were registered
     */
    boolean unregisterHook(Method method);
    
    /**
     * Checks if the specified method has any registered hooks.
     * 
     * @param method the method to check
     * @return true if the method has entry or exit hooks
     */
    boolean hasHooks(Method method);
    
    /**
     * Gets the entry hook for the specified method.
     * 
     * @param method the method to query
     * @return the entry hook, or null if none is registered
     */
    EntryHook getEntryHook(Method method);
    
    /**
     * Gets the exit hook for the specified method.
     * 
     * @param method the method to query
     * @return the exit hook, or null if none is registered
     */
    ExitHook getExitHook(Method method);
    
    /**
     * Returns statistics about registered hooks.
     * 
     * @return hook statistics
     */
    HookStatistics getStatistics();
    
    /**
     * Hook statistics.
     */
    interface HookStatistics {
        /**
         * @return number of methods with entry hooks
         */
        int getEntryHookCount();
        
        /**
         * @return number of methods with exit hooks
         */
        int getExitHookCount();
        
        /**
         * @return total number of hook invocations
         */
        long getTotalInvocations();
        
        /**
         * @return number of hook failures (exceptions caught)
         */
        long getFailureCount();
    }
}
