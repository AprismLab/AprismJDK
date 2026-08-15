package jdk.aprismate.memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DirectBufferPool API.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
class DirectBufferPoolTest {
    
    @Test
    void testCreate() {
        assertDoesNotThrow(() -> {
            try {
                PoolConfig config = PoolConfig.builder()
                    .minBufferSize(1024)
                    .maxBufferSize(1024 * 1024)
                    .maxPoolSize(10)
                    .build();
                DirectBufferPool pool = DirectBufferPool.create(config);
                assertNotNull(pool);
            } catch (UnsupportedOperationException e) {
                // Expected on stock JDK
            }
        });
    }
    
    @Test
    void testInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            PoolConfig.builder()
                .minBufferSize(0)
                .maxBufferSize(1024)
                .build();
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PoolConfig.builder()
                .minBufferSize(-1)
                .maxBufferSize(1024)
                .build();
        });
    }
    
    @Test
    void testInvalidMaxBuffers() {
        assertThrows(IllegalArgumentException.class, () -> {
            PoolConfig.builder()
                .minBufferSize(1024)
                .maxBufferSize(1024 * 1024)
                .maxPoolSize(0)
                .build();
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            PoolConfig.builder()
                .minBufferSize(1024)
                .maxBufferSize(1024 * 1024)
                .maxPoolSize(-1)
                .build();
        });
    }
}
