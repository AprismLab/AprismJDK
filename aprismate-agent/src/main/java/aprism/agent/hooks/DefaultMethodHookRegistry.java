package aprism.agent.hooks;

import jdk.aprismate.agent.EntryHook;
import jdk.aprismate.agent.ExitHook;
import jdk.aprismate.agent.MethodHookRegistry;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of MethodHookRegistry.
 * 
 * <p>This implementation uses a thread-safe map to track hooks and provides
 * fail-safe behavior by catching and logging exceptions from hooks.
 * 
 * @since v26.1-Alpha.4
 */
public class DefaultMethodHookRegistry implements MethodHookRegistry {
    
    private final Map<Method, HookPair> hooks = new ConcurrentHashMap<>();
    private final AtomicLong totalInvocations = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    
    private static class HookPair {
        final EntryHook entryHook;
        final ExitHook exitHook;
        
        HookPair(EntryHook entryHook, ExitHook exitHook) {
            this.entryHook = entryHook;
            this.exitHook = exitHook;
        }
        
        HookPair withEntry(EntryHook entry) {
            return new HookPair(entry, this.exitHook);
        }
        
        HookPair withExit(ExitHook exit) {
            return new HookPair(this.entryHook, exit);
        }
    }
    
    @Override
    public void registerEntryHook(Method method, EntryHook hook) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }
        if (hook == null) {
            throw new IllegalArgumentException("hook cannot be null");
        }
        
        hooks.compute(method, (k, v) -> {
            if (v == null) {
                return new HookPair(hook, null);
            } else {
                return v.withEntry(hook);
            }
        });
    }
    
    @Override
    public void registerExitHook(Method method, ExitHook hook) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }
        if (hook == null) {
            throw new IllegalArgumentException("hook cannot be null");
        }
        
        hooks.compute(method, (k, v) -> {
            if (v == null) {
                return new HookPair(null, hook);
            } else {
                return v.withExit(hook);
            }
        });
    }
    
    @Override
    public boolean unregisterHook(Method method) {
        return hooks.remove(method) != null;
    }
    
    @Override
    public boolean hasHooks(Method method) {
        return hooks.containsKey(method);
    }
    
    @Override
    public EntryHook getEntryHook(Method method) {
        HookPair pair = hooks.get(method);
        return pair != null ? pair.entryHook : null;
    }
    
    @Override
    public ExitHook getExitHook(Method method) {
        HookPair pair = hooks.get(method);
        return pair != null ? pair.exitHook : null;
    }
    
    @Override
    public HookStatistics getStatistics() {
        return new HookStatistics() {
            @Override
            public int getEntryHookCount() {
                return (int) hooks.values().stream()
                    .filter(p -> p.entryHook != null)
                    .count();
            }
            
            @Override
            public int getExitHookCount() {
                return (int) hooks.values().stream()
                    .filter(p -> p.exitHook != null)
                    .count();
            }
            
            @Override
            public long getTotalInvocations() {
                return totalInvocations.get();
            }
            
            @Override
            public long getFailureCount() {
                return failureCount.get();
            }
        };
    }
    
    /**
     * Invokes the entry hook for a method. This method is called by the VM
     * instrumentation code.
     * 
     * @param method the method being invoked
     * @param target the target object (null for static methods)
     * @param args the method arguments
     */
    public void invokeEntryHook(Method method, Object target, Object[] args) {
        HookPair pair = hooks.get(method);
        if (pair != null && pair.entryHook != null) {
            totalInvocations.incrementAndGet();
            try {
                pair.entryHook.onEntry(method, target, args);
            } catch (Throwable t) {
                failureCount.incrementAndGet();
                // Log but don't propagate - fail-safe behavior
                System.err.println("Entry hook failed for " + method + ": " + t.getMessage());
            }
        }
    }
    
    /**
     * Invokes the exit hook for a method. This method is called by the VM
     * instrumentation code.
     * 
     * @param method the method that was invoked
     * @param target the target object (null for static methods)
     * @param returnValue the return value (null for void or exception)
     * @param exception the exception thrown (null if no exception)
     */
    public void invokeExitHook(Method method, Object target, Object returnValue, Throwable exception) {
        HookPair pair = hooks.get(method);
        if (pair != null && pair.exitHook != null) {
            totalInvocations.incrementAndGet();
            try {
                pair.exitHook.onExit(method, target, returnValue, exception);
            } catch (Throwable t) {
                failureCount.incrementAndGet();
                // Log but don't propagate - fail-safe behavior
                System.err.println("Exit hook failed for " + method + ": " + t.getMessage());
            }
        }
    }
}
