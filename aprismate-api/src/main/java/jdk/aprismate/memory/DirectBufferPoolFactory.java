package jdk.aprismate.memory;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Factory for creating DirectBufferPool instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class DirectBufferPoolFactory {
    
    private DirectBufferPoolFactory() {
        // No instantiation
    }
    
    /**
     * Creates a buffer pool with the specified configuration.
     */
    static DirectBufferPool create(PoolConfig config) {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.memory.DirectBufferPoolImpl");
            return (DirectBufferPool) implClass.getConstructor(PoolConfig.class).newInstance(config);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubDirectBufferPool(config);
        }
    }
    
    /**
     * Stub implementation for when agent is not available.
     */
    private static class StubDirectBufferPool implements DirectBufferPool {
        
        private final PoolConfig config;
        private volatile boolean closed = false;
        
        StubDirectBufferPool(PoolConfig config) {
            this.config = config;
        }
        
        @Override
        public ByteBuffer acquire(int capacity) {
            checkClosed();
            // Fallback: allocate directly (no pooling)
            return ByteBuffer.allocateDirect(capacity);
        }
        
        @Override
        public ByteBuffer acquire(int capacity, long timeout, TimeUnit unit) throws InterruptedException {
            checkClosed();
            return ByteBuffer.allocateDirect(capacity);
        }
        
        @Override
        public void release(ByteBuffer buffer) {
            checkClosed();
            // No-op: let GC handle it
        }
        
        @Override
        public PoolStats stats() {
            return new StubPoolStats();
        }
        
        @Override
        public int checkLeaks() {
            return 0;
        }
        
        @Override
        public void clear() {
            // No-op
        }
        
        @Override
        public void close() {
            closed = true;
        }
        
        private void checkClosed() {
            if (closed) {
                throw new IllegalStateException("Pool is closed");
            }
        }
    }
    
    /**
     * Stub stats implementation.
     */
    private static class StubPoolStats implements PoolStats {
        
        @Override
        public long totalAcquires() {
            return 0;
        }
        
        @Override
        public long totalReleases() {
            return 0;
        }
        
        @Override
        public long poolHits() {
            return 0;
        }
        
        @Override
        public long poolMisses() {
            return 0;
        }
        
        @Override
        public int pooledBuffers() {
            return 0;
        }
        
        @Override
        public int activeBuffers() {
            return 0;
        }
        
        @Override
        public long pooledMemory() {
            return 0;
        }
        
        @Override
        public long activeMemory() {
            return 0;
        }
        
        @Override
        public int peakActiveBuffers() {
            return 0;
        }
        
        @Override
        public long leakCount() {
            return 0;
        }
        
        @Override
        public double poolUtilization() {
            return 0.0;
        }
        
        @Override
        public long averageBufferSize() {
            return 0;
        }
        
        @Override
        public void reset() {
            // No-op
        }
    }
}
