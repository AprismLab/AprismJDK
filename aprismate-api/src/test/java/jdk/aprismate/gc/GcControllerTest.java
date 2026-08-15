package jdk.aprismate.gc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

/**
 * Tests for GcController API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class GcControllerTest {
    
    @Test
    void testTriggerYoungGc() {
        assertDoesNotThrow(() -> {
            GcController.triggerYoungGc();
        });
    }
    
    @Test
    void testTriggerFullGc() {
        assertDoesNotThrow(() -> {
            GcController.triggerFullGc();
        });
    }
    
    @Test
    void testTriggerConcurrentMark() {
        assertDoesNotThrow(() -> {
            try {
                GcController.triggerConcurrentMark();
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK or non-concurrent GC
            }
        });
    }
    
    @Test
    void testSetConcurrentThreads() {
        assertDoesNotThrow(() -> {
            try {
                GcController.setConcurrentThreads(4);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testSetParallelThreads() {
        assertDoesNotThrow(() -> {
            try {
                GcController.setParallelThreads(4);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testInvalidThreadCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            GcController.setConcurrentThreads(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            GcController.setConcurrentThreads(-1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            GcController.setParallelThreads(0);
        });
    }
    
    @Test
    void testSetYoungGenSize() {
        assertDoesNotThrow(() -> {
            try {
                GcController.setYoungGenSize(512 * 1024 * 1024);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testInvalidGenSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            GcController.setYoungGenSize(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            GcController.setYoungGenSize(-1);
        });
    }
    
    @Test
    void testSetPauseTarget() {
        assertDoesNotThrow(() -> {
            try {
                GcController.setPauseTarget(Duration.ofMillis(10));
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK or GC without pause target
            }
        });
    }
    
    @Test
    void testNullPauseTarget() {
        assertThrows(NullPointerException.class, () -> {
            GcController.setPauseTarget(null);
        });
    }
    
    @Test
    void testGetStats() {
        assertDoesNotThrow(() -> {
            GcStats stats = GcController.getStats();
            assertNotNull(stats);
            assertTrue(stats.heapMax() > 0);
        });
    }
    
    @Test
    void testGcType() {
        assertDoesNotThrow(() -> {
            String type = GcController.gcType();
            assertNotNull(type);
        });
    }
    
    @Test
    void testSupportsConcurrentMarking() {
        assertDoesNotThrow(() -> {
            boolean supports = GcController.supportsConcurrentMarking();
            // Result depends on GC type
        });
    }
    
    @Test
    void testExplicitGcControl() {
        assertDoesNotThrow(() -> {
            boolean initialState = GcController.isExplicitGcEnabled();
            
            try {
                GcController.disableExplicitGc();
                // May throw on stock JDK
            } catch (UnsupportedOperationException e) {
                // Expected
            }
            
            try {
                GcController.enableExplicitGc();
            } catch (UnsupportedOperationException e) {
                // Expected
            }
        });
    }
    
    @Test
    void testAddListener() {
        assertDoesNotThrow(() -> {
            GcController.addListener(event -> {
                // Listener callback
            });
        });
    }
    
    @Test
    void testNullListener() {
        assertThrows(NullPointerException.class, () -> {
            GcController.addListener(null);
        });
        
        assertThrows(NullPointerException.class, () -> {
            GcController.removeListener(null);
        });
    }
}
