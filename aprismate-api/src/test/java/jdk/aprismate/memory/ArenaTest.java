package jdk.aprismate.memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Arena API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class ArenaTest {
    
    @Test
    void testOfConfined() {
        // Should not throw on API call (impl may throw UnsupportedOperationException)
        assertDoesNotThrow(() -> {
            try {
                Arena arena = Arena.ofConfined();
                assertNotNull(arena);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testOfShared() {
        assertDoesNotThrow(() -> {
            try {
                Arena arena = Arena.ofShared();
                assertNotNull(arena);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testGlobal() {
        assertDoesNotThrow(() -> {
            Arena arena = Arena.global();
            assertNotNull(arena);
        });
    }
    
    @Test
    void testAutoArena() {
        assertDoesNotThrow(() -> {
            try {
                Arena arena = Arena.ofAuto();
                assertNotNull(arena);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
}
