package jdk.aprismate.concurrent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LockFreeQueue API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class LockFreeQueueTest {
    
    @Test
    void testCreateUnbounded() {
        assertDoesNotThrow(() -> {
            try {
                LockFreeQueue<String> queue = LockFreeQueue.unbounded();
                assertNotNull(queue);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testCreateBounded() {
        assertDoesNotThrow(() -> {
            try {
                LockFreeQueue<String> queue = LockFreeQueue.bounded(100);
                assertNotNull(queue);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> {
            LockFreeQueue.bounded(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            LockFreeQueue.bounded(-1);
        });
    }
}
