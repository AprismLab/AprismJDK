package jdk.aprismate.ffi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StructLayout API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class StructLayoutTest {
    
    @Test
    void testDefine() {
        assertDoesNotThrow(() -> {
            StructLayout.Builder builder = StructLayout.define("Point");
            assertNotNull(builder);
        });
    }
    
    @Test
    void testNullName() {
        assertThrows(NullPointerException.class, () -> {
            StructLayout.define(null);
        });
    }
    
    @Test
    void testEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            StructLayout.define("");
        });
    }
}
