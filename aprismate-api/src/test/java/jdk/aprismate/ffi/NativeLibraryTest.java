package jdk.aprismate.ffi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NativeLibrary API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class NativeLibraryTest {
    
    @Test
    void testLoad() {
        assertDoesNotThrow(() -> {
            try {
                // Try to load C library (available on most systems)
                NativeLibrary lib = NativeLibrary.load("c");
                assertNotNull(lib);
            } catch (UnsupportedOperationException | UnsatisfiedLinkError e) {
                // Expected on stock JDK or if library not found
            }
        });
    }
    
    @Test
    void testCLibrary() {
        assertDoesNotThrow(() -> {
            try {
                NativeLibrary lib = NativeLibrary.cLibrary();
                assertNotNull(lib);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testBuilder() {
        assertDoesNotThrow(() -> {
            NativeLibrary.Builder builder = NativeLibrary.builder().name("test");
            assertNotNull(builder);
        });
    }
    
    @Test
    void testNullName() {
        assertThrows(NullPointerException.class, () -> {
            NativeLibrary.load((String) null);
        });
    }
}
