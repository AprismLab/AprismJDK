package jdk.aprismate.concurrent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FiberScheduler API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class FiberSchedulerTest {
    
    @Test
    void testCreate() {
        assertDoesNotThrow(() -> {
            try {
                FiberScheduler scheduler = FiberScheduler.create();
                assertNotNull(scheduler);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
}
