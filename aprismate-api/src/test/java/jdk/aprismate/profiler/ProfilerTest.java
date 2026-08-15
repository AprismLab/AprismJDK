package jdk.aprismate.profiler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

/**
 * Tests for Profiler API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class ProfilerTest {
    
    @Test
    void testCpuProfiler() {
        assertDoesNotThrow(() -> {
            Profiler.CpuProfiler profiler = Profiler.cpu();
            assertNotNull(profiler);
            
            profiler.interval(Duration.ofMillis(10));
            profiler.includeNative();
            
            try {
                profiler.start();
                // Should throw on stock JDK
                fail("Expected UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                // Expected
            }
        });
    }
    
    @Test
    void testAllocationProfiler() {
        assertDoesNotThrow(() -> {
            Profiler.AllocationProfiler profiler = Profiler.allocation();
            assertNotNull(profiler);
            
            profiler.threshold(1024);
            profiler.includeSize();
            
            try {
                profiler.start();
                fail("Expected UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                // Expected
            }
        });
    }
    
    @Test
    void testWallClockProfiler() {
        assertDoesNotThrow(() -> {
            Profiler.WallClockProfiler profiler = Profiler.wallClock();
            assertNotNull(profiler);
            
            profiler.interval(Duration.ofMillis(10));
            
            try {
                profiler.start();
                fail("Expected UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                // Expected
            }
        });
    }
    
    @Test
    void testLockProfiler() {
        assertDoesNotThrow(() -> {
            Profiler.LockProfiler profiler = Profiler.lock();
            assertNotNull(profiler);
            
            profiler.threshold(Duration.ofMillis(1));
            
            try {
                profiler.start();
                fail("Expected UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                // Expected
            }
        });
    }
    
    @Test
    void testIsActive() {
        assertDoesNotThrow(() -> {
            boolean active = Profiler.isActive();
            // Result depends on profiling state
        });
    }
    
    @Test
    void testMode() {
        assertDoesNotThrow(() -> {
            Profiler.ProfilingMode mode = Profiler.mode();
            // May be null if not profiling
        });
    }
    
    @Test
    void testStopWithoutStart() {
        assertThrows(IllegalStateException.class, () -> {
            Profiler.stop();
        });
    }
}
