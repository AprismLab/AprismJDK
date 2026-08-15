package jdk.aprismate.gc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GcStats API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class GcStatsTest {
    
    @Test
    void testBasicStats() {
        GcStats stats = GcController.getStats();
        
        assertNotNull(stats);
        assertTrue(stats.totalPauses() >= 0);
        assertTrue(stats.youngGcCount() >= 0);
        assertTrue(stats.oldGcCount() >= 0);
        assertTrue(stats.fullGcCount() >= 0);
    }
    
    @Test
    void testHeapStats() {
        GcStats stats = GcController.getStats();
        
        assertTrue(stats.heapUsed() >= 0);
        assertTrue(stats.heapCapacity() >= 0);
        assertTrue(stats.heapMax() > 0);
        assertTrue(stats.heapUsed() <= stats.heapCapacity());
        assertTrue(stats.heapCapacity() <= stats.heapMax());
    }
    
    @Test
    void testUtilization() {
        GcStats stats = GcController.getStats();
        
        double heapUtil = stats.heapUtilization();
        assertTrue(heapUtil >= 0.0 && heapUtil <= 100.0);
        
        double youngUtil = stats.youngGenUtilization();
        assertTrue(youngUtil >= 0.0 && youngUtil <= 100.0);
        
        double oldUtil = stats.oldGenUtilization();
        assertTrue(oldUtil >= 0.0 && oldUtil <= 100.0);
    }
    
    @Test
    void testGcOverhead() {
        GcStats stats = GcController.getStats();
        
        double overhead = stats.gcOverhead();
        assertTrue(overhead >= 0.0 && overhead <= 100.0);
    }
    
    @Test
    void testReset() {
        GcStats stats = GcController.getStats();
        
        assertDoesNotThrow(() -> {
            stats.reset();
        });
    }
}
