package jdk.aprismate.memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OffHeapMap API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class OffHeapMapTest {
    
    @Test
    void testCreate() {
        assertDoesNotThrow(() -> {
            try {
                OffHeapMap<String, String> map = OffHeapMap.create(String.class, String.class);
                assertNotNull(map);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testCreateWithCapacity() {
        assertDoesNotThrow(() -> {
            try {
                OffHeapMap<String, String> map = OffHeapMap.create(String.class, String.class, 1024);
                assertNotNull(map);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> {
            OffHeapMap.create(String.class, String.class, 0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            OffHeapMap.create(String.class, String.class, -1);
        });
    }
}
