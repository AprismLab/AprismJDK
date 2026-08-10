package jdk.aprismate.event.lifecycle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for lifecycle events.
 */
class LifecycleEventTest {
    
    @Test
    void startupEventShouldStoreArgs() {
        String[] args = {"arg1", "arg2", "arg3"};
        StartupEvent event = new StartupEvent(args);
        
        assertArrayEquals(args, event.getArgs(), "Startup event should store arguments");
    }
    
    @Test
    void startupEventShouldHandleNullArgs() {
        StartupEvent event = new StartupEvent(null);
        
        assertNotNull(event.getArgs(), "getArgs() should never return null");
        assertEquals(0, event.getArgs().length, "Null args should be converted to empty array");
    }
    
    @Test
    void startupEventShouldCloneArgs() {
        String[] args = {"arg1", "arg2"};
        StartupEvent event = new StartupEvent(args);
        
        args[0] = "modified";
        
        assertEquals("arg1", event.getArgs()[0], 
            "Modifying original array should not affect event args");
    }
    
    @Test
    void startupEventGetArgsShouldReturnCopy() {
        StartupEvent event = new StartupEvent(new String[]{"arg1"});
        String[] args1 = event.getArgs();
        String[] args2 = event.getArgs();
        
        assertNotSame(args1, args2, "getArgs() should return a new array each time");
    }
    
    @Test
    void shutdownEventShouldStoreReason() {
        ShutdownEvent event = new ShutdownEvent(ShutdownEvent.ShutdownReason.NORMAL);
        
        assertEquals(ShutdownEvent.ShutdownReason.NORMAL, event.getReason());
    }
    
    @Test
    void shutdownEventShouldRejectNullReason() {
        assertThrows(NullPointerException.class, () -> {
            new ShutdownEvent(null);
        }, "ShutdownEvent should reject null reason");
    }
    
    @Test
    void shutdownEventShouldBeCancellable() {
        ShutdownEvent event = new ShutdownEvent(ShutdownEvent.ShutdownReason.NORMAL);
        
        assertTrue(event.isCancellable(), "ShutdownEvent should be cancellable");
        assertFalse(event.isCancelled(), "ShutdownEvent should start not cancelled");
    }
    
    @Test
    void shutdownEventCanBeCancelled() {
        ShutdownEvent event = new ShutdownEvent(ShutdownEvent.ShutdownReason.USER_REQUESTED);
        
        event.setCancelled(true);
        assertTrue(event.isCancelled(), "ShutdownEvent should be cancellable");
    }
    
    @Test
    void lifecycleEventShouldHaveTimestamp() {
        long before = System.currentTimeMillis();
        StartupEvent event = new StartupEvent(new String[0]);
        long after = System.currentTimeMillis();
        
        assertTrue(event.getTimestamp() >= before && event.getTimestamp() <= after,
            "Event timestamp should be within expected range");
    }
    
    @Test
    void shutdownReasonShouldHaveAllValues() {
        ShutdownEvent.ShutdownReason[] reasons = ShutdownEvent.ShutdownReason.values();
        
        assertEquals(4, reasons.length, "ShutdownReason should have 4 values");
        assertTrue(containsReason(reasons, ShutdownEvent.ShutdownReason.NORMAL));
        assertTrue(containsReason(reasons, ShutdownEvent.ShutdownReason.USER_REQUESTED));
        assertTrue(containsReason(reasons, ShutdownEvent.ShutdownReason.ERROR));
        assertTrue(containsReason(reasons, ShutdownEvent.ShutdownReason.SIGNAL));
    }
    
    private boolean containsReason(ShutdownEvent.ShutdownReason[] reasons, 
                                   ShutdownEvent.ShutdownReason target) {
        for (ShutdownEvent.ShutdownReason reason : reasons) {
            if (reason == target) {
                return true;
            }
        }
        return false;
    }
}
