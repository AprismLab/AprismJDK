package jdk.aprismate.memory;

import java.lang.foreign.MemorySegment;

/**
 * Factory for creating Arena instances.
 * 
 * <p>This is an internal factory class that provides implementations
 * for different arena types. It delegates to the agent implementation
 * when available, and provides stub implementations otherwise.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class ArenaFactory {
    
    private static final Arena GLOBAL = new GlobalArena();
    
    private ArenaFactory() {
        // No instantiation
    }
    
    /**
     * Creates a confined arena.
     */
    static Arena createConfined() {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.memory.ConfinedArena");
            return (Arena) implClass.getMethod("create").invoke(null);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubArena("confined");
        }
    }
    
    /**
     * Creates a shared arena.
     */
    static Arena createShared() {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.memory.SharedArena");
            return (Arena) implClass.getMethod("create").invoke(null);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubArena("shared");
        }
    }
    
    /**
     * Creates an auto-managed arena.
     */
    static Arena createAuto() {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.memory.AutoArena");
            return (Arena) implClass.getMethod("create").invoke(null);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubArena("auto");
        }
    }
    
    /**
     * Returns the global arena.
     */
    static Arena getGlobal() {
        return GLOBAL;
    }
    
    /**
     * Stub implementation for when agent is not available.
     */
    private static class StubArena implements Arena {
        
        private final String type;
        private volatile boolean closed = false;
        
        StubArena(String type) {
            this.type = type;
        }
        
        @Override
        public MemorySegment allocate(long size) {
            checkClosed();
            throw new UnsupportedOperationException(
                "Arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public MemorySegment allocate(long size, long alignment) {
            checkClosed();
            throw new UnsupportedOperationException(
                "Arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public <T> MemorySegment allocate(Class<T> type, long count) {
            checkClosed();
            throw new UnsupportedOperationException(
                "Arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public MemorySegment allocateFrom(byte[] bytes) {
            checkClosed();
            throw new UnsupportedOperationException(
                "Arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public long allocated() {
            return 0;
        }
        
        @Override
        public long peak() {
            return 0;
        }
        
        @Override
        public long allocationCount() {
            return 0;
        }
        
        @Override
        public boolean isClosed() {
            return closed;
        }
        
        @Override
        public void close() {
            closed = true;
        }
        
        private void checkClosed() {
            if (closed) {
                throw new IllegalStateException("Arena is closed");
            }
        }
        
        @Override
        public String toString() {
            return "StubArena[" + type + ", closed=" + closed + "]";
        }
    }
    
    /**
     * Global arena that never closes.
     */
    private static class GlobalArena implements Arena {
        
        @Override
        public MemorySegment allocate(long size) {
            throw new UnsupportedOperationException(
                "Global arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public MemorySegment allocate(long size, long alignment) {
            throw new UnsupportedOperationException(
                "Global arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public <T> MemorySegment allocate(Class<T> type, long count) {
            throw new UnsupportedOperationException(
                "Global arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public MemorySegment allocateFrom(byte[] bytes) {
            throw new UnsupportedOperationException(
                "Global arena allocation requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public long allocated() {
            return 0;
        }
        
        @Override
        public long peak() {
            return 0;
        }
        
        @Override
        public long allocationCount() {
            return 0;
        }
        
        @Override
        public boolean isClosed() {
            return false;
        }
        
        @Override
        public void close() {
            // Global arena never closes
        }
        
        @Override
        public String toString() {
            return "GlobalArena[never closes]";
        }
    }
}
