package aprism.agent.hooks;

import jdk.aprismate.agent.EntryHook;
import jdk.aprismate.agent.ExitHook;
import jdk.aprismate.agent.MethodHookRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DefaultMethodHookRegistry.
 */
class DefaultMethodHookRegistryTest {
    
    private MethodHookRegistry registry;
    private Method testMethod;
    
    // Test target class
    static class TestTarget {
        public static int staticMethod(int x) {
            return x * 2;
        }
        
        public int instanceMethod(int x) {
            return x * 3;
        }
        
        public void voidMethod() {
            // Do nothing
        }
        
        public int throwingMethod() {
            throw new RuntimeException("Expected exception");
        }
    }
    
    @BeforeEach
    void setUp() throws Exception {
        registry = new DefaultMethodHookRegistry();
        testMethod = TestTarget.class.getMethod("instanceMethod", int.class);
    }
    
    @Test
    void testRegisterEntryHook() {
        List<String> calls = new ArrayList<>();
        EntryHook hook = (method, target, args) -> calls.add("entry");
        
        registry.registerEntryHook(testMethod, hook);
        
        assertTrue(registry.hasHooks(testMethod));
        assertNotNull(registry.getEntryHook(testMethod));
        assertNull(registry.getExitHook(testMethod));
    }
    
    @Test
    void testRegisterExitHook() {
        List<String> calls = new ArrayList<>();
        ExitHook hook = (method, target, returnValue, exception) -> calls.add("exit");
        
        registry.registerExitHook(testMethod, hook);
        
        assertTrue(registry.hasHooks(testMethod));
        assertNull(registry.getEntryHook(testMethod));
        assertNotNull(registry.getExitHook(testMethod));
    }
    
    @Test
    void testRegisterBothHooks() {
        List<String> calls = new ArrayList<>();
        EntryHook entryHook = (method, target, args) -> calls.add("entry");
        ExitHook exitHook = (method, target, returnValue, exception) -> calls.add("exit");
        
        registry.registerEntryHook(testMethod, entryHook);
        registry.registerExitHook(testMethod, exitHook);
        
        assertTrue(registry.hasHooks(testMethod));
        assertNotNull(registry.getEntryHook(testMethod));
        assertNotNull(registry.getExitHook(testMethod));
    }
    
    @Test
    void testUnregisterHook() {
        EntryHook hook = (method, target, args) -> {};
        
        registry.registerEntryHook(testMethod, hook);
        assertTrue(registry.hasHooks(testMethod));
        
        boolean removed = registry.unregisterHook(testMethod);
        assertTrue(removed);
        assertFalse(registry.hasHooks(testMethod));
        
        // Unregistering again should return false
        removed = registry.unregisterHook(testMethod);
        assertFalse(removed);
    }
    
    @Test
    void testInvokeEntryHook() {
        List<String> calls = new ArrayList<>();
        List<Object[]> capturedArgs = new ArrayList<>();
        
        EntryHook hook = (method, target, args) -> {
            calls.add("entry");
            capturedArgs.add(args);
        };
        
        registry.registerEntryHook(testMethod, hook);
        
        TestTarget targetInstance = new TestTarget();
        Object[] args = {42};
        
        ((DefaultMethodHookRegistry) registry).invokeEntryHook(testMethod, targetInstance, args);
        
        assertEquals(1, calls.size());
        assertEquals("entry", calls.get(0));
        assertEquals(1, capturedArgs.size());
        assertArrayEquals(args, capturedArgs.get(0));
    }
    
    @Test
    void testInvokeExitHook() {
        List<String> calls = new ArrayList<>();
        List<Object> capturedReturns = new ArrayList<>();
        
        ExitHook hook = (method, target, returnValue, exception) -> {
            calls.add("exit");
            capturedReturns.add(returnValue);
        };
        
        registry.registerExitHook(testMethod, hook);
        
        TestTarget targetInstance = new TestTarget();
        Object returnValue = 126;
        
        ((DefaultMethodHookRegistry) registry).invokeExitHook(testMethod, targetInstance, returnValue, null);
        
        assertEquals(1, calls.size());
        assertEquals("exit", calls.get(0));
        assertEquals(1, capturedReturns.size());
        assertEquals(126, capturedReturns.get(0));
    }
    
    @Test
    void testInvokeExitHookWithException() {
        List<Throwable> capturedExceptions = new ArrayList<>();
        
        ExitHook hook = (method, target, returnValue, exception) -> {
            capturedExceptions.add(exception);
        };
        
        registry.registerExitHook(testMethod, hook);
        
        RuntimeException testException = new RuntimeException("Test");
        ((DefaultMethodHookRegistry) registry).invokeExitHook(testMethod, null, null, testException);
        
        assertEquals(1, capturedExceptions.size());
        assertSame(testException, capturedExceptions.get(0));
    }
    
    @Test
    void testHookFailureSafety() {
        // Hook that throws an exception
        EntryHook badHook = (method, target, args) -> {
            throw new RuntimeException("Hook failure");
        };
        
        registry.registerEntryHook(testMethod, badHook);
        
        // Should not throw - failure is caught and logged
        assertDoesNotThrow(() -> 
            ((DefaultMethodHookRegistry) registry).invokeEntryHook(testMethod, null, new Object[0])
        );
        
        // Should track the failure
        assertTrue(registry.getStatistics().getFailureCount() > 0);
    }
    
    @Test
    void testStatistics() {
        Method method1 = testMethod;
        Method method2;
        try {
            method2 = TestTarget.class.getMethod("voidMethod");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        EntryHook entryHook = (method, target, args) -> {};
        ExitHook exitHook = (method, target, returnValue, exception) -> {};
        
        registry.registerEntryHook(method1, entryHook);
        registry.registerExitHook(method1, exitHook);
        registry.registerEntryHook(method2, entryHook);
        
        MethodHookRegistry.HookStatistics stats = registry.getStatistics();
        assertEquals(2, stats.getEntryHookCount());
        assertEquals(1, stats.getExitHookCount());
        assertEquals(0, stats.getTotalInvocations());
        
        // Invoke a hook
        ((DefaultMethodHookRegistry) registry).invokeEntryHook(method1, null, new Object[0]);
        
        stats = registry.getStatistics();
        assertEquals(1, stats.getTotalInvocations());
    }
    
    @Test
    void testNullMethodThrows() {
        EntryHook hook = (method, target, args) -> {};
        
        assertThrows(IllegalArgumentException.class, () -> 
            registry.registerEntryHook(null, hook)
        );
    }
    
    @Test
    void testNullHookThrows() {
        assertThrows(IllegalArgumentException.class, () -> 
            registry.registerEntryHook(testMethod, null)
        );
    }
    
    @Test
    void testReplaceEntryHook() {
        List<String> calls = new ArrayList<>();
        
        EntryHook hook1 = (method, target, args) -> calls.add("hook1");
        EntryHook hook2 = (method, target, args) -> calls.add("hook2");
        
        registry.registerEntryHook(testMethod, hook1);
        registry.registerEntryHook(testMethod, hook2);
        
        ((DefaultMethodHookRegistry) registry).invokeEntryHook(testMethod, null, new Object[0]);
        
        // Should only call the second hook
        assertEquals(1, calls.size());
        assertEquals("hook2", calls.get(0));
    }
    
    @Test
    void testStaticMethodHook() throws Exception {
        Method staticMethod = TestTarget.class.getMethod("staticMethod", int.class);
        List<Object> capturedTargets = new ArrayList<>();
        
        EntryHook hook = (method, target, args) -> capturedTargets.add(target);
        
        registry.registerEntryHook(staticMethod, hook);
        ((DefaultMethodHookRegistry) registry).invokeEntryHook(staticMethod, null, new Object[]{10});
        
        assertEquals(1, capturedTargets.size());
        assertNull(capturedTargets.get(0)); // Static method has null target
    }
}
