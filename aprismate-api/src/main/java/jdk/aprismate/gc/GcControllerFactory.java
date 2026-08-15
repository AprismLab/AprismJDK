package jdk.aprismate.gc;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * Factory for GC controller operations.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class GcControllerFactory {
    
    private static final Set<Consumer<GcEvent>> listeners = new HashSet<>();
    
    private GcControllerFactory() {
        // No instantiation
    }
    
    static void triggerYoungGc() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("triggerYoungGc").invoke(null);
        } catch (Exception e) {
            // Fallback to System.gc()
            System.gc();
        }
    }
    
    static void triggerFullGc() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("triggerFullGc").invoke(null);
        } catch (Exception e) {
            // Fallback to System.gc()
            System.gc();
        }
    }
    
    static void triggerConcurrentMark() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("triggerConcurrentMark").invoke(null);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "Concurrent marking control requires AprismJDK");
        }
    }
    
    static void triggerConcurrentCompaction() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("triggerConcurrentCompaction").invoke(null);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "Concurrent compaction control requires AprismJDK");
        }
    }
    
    static void setConcurrentThreads(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("setConcurrentThreads", int.class).invoke(null, threads);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "GC thread control requires AprismJDK");
        }
    }
    
    static void setParallelThreads(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("setParallelThreads", int.class).invoke(null, threads);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "GC thread control requires AprismJDK");
        }
    }
    
    static void setYoungGenSize(long bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("setYoungGenSize", long.class).invoke(null, bytes);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "Generation size control requires AprismJDK");
        }
    }
    
    static void setOldGenSize(long bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("setOldGenSize", long.class).invoke(null, bytes);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "Generation size control requires AprismJDK");
        }
    }
    
    static void setPauseTarget(Duration target) {
        Objects.requireNonNull(target, "target");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("setPauseTarget", Duration.class).invoke(null, target);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "Pause target control requires AprismJDK and G1/Shenandoah GC");
        }
    }
    
    static void setG1RegionSize(int bytes) {
        if (bytes < 1024 * 1024 || bytes > 32 * 1024 * 1024 || Integer.bitCount(bytes) != 1) {
            throw new IllegalArgumentException(
                "G1 region size must be power of 2 between 1MB and 32MB");
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("setG1RegionSize", int.class).invoke(null, bytes);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "G1 region size control requires AprismJDK with G1GC");
        }
    }
    
    static void addListener(Consumer<GcEvent> listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (listeners) {
            listeners.add(listener);
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("addListener", Consumer.class).invoke(null, listener);
        } catch (Exception e) {
            // Fallback: listener won't receive events on stock JDK
        }
    }
    
    static void removeListener(Consumer<GcEvent> listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (listeners) {
            listeners.remove(listener);
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("removeListener", Consumer.class).invoke(null, listener);
        } catch (Exception e) {
            // Fallback: no-op
        }
    }
    
    static GcStats getStats() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            return (GcStats) implClass.getMethod("getStats").invoke(null);
        } catch (Exception e) {
            return new StubGcStats();
        }
    }
    
    static String gcType() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            return (String) implClass.getMethod("gcType").invoke(null);
        } catch (Exception e) {
            // Try to detect from system properties
            String gcName = System.getProperty("java.vm.name", "");
            if (gcName.contains("ZGC")) return "ZGC";
            if (gcName.contains("Shenandoah")) return "Shenandoah";
            return "Unknown";
        }
    }
    
    static boolean supportsConcurrentMarking() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            return (boolean) implClass.getMethod("supportsConcurrentMarking").invoke(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    static boolean supportsConcurrentCompaction() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            return (boolean) implClass.getMethod("supportsConcurrentCompaction").invoke(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    static void disableExplicitGc() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("disableExplicitGc").invoke(null);
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                "Explicit GC control requires AprismJDK");
        }
    }
    
    static void enableExplicitGc() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            implClass.getMethod("enableExplicitGc").invoke(null);
        } catch (Exception e) {
            // Fallback: no-op (already enabled by default)
        }
    }
    
    static boolean isExplicitGcEnabled() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.gc.GcControllerImpl");
            return (boolean) implClass.getMethod("isExplicitGcEnabled").invoke(null);
        } catch (Exception e) {
            // Assume enabled on stock JDK
            return !System.getProperty("java.vm.info", "").contains("-XX:+DisableExplicitGC");
        }
    }
    
    /**
     * Stub GcStats implementation.
     */
    private static class StubGcStats implements GcStats {
        
        @Override
        public long totalPauses() { return 0; }
        
        @Override
        public long youngGcCount() { return 0; }
        
        @Override
        public long oldGcCount() { return 0; }
        
        @Override
        public long fullGcCount() { return 0; }
        
        @Override
        public Duration totalPauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration youngGcTime() { return Duration.ZERO; }
        
        @Override
        public Duration oldGcTime() { return Duration.ZERO; }
        
        @Override
        public Duration averagePauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration maxPauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration minPauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration p50PauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration p95PauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration p99PauseTime() { return Duration.ZERO; }
        
        @Override
        public Duration p999PauseTime() { return Duration.ZERO; }
        
        @Override
        public long totalAllocatedBytes() { return 0; }
        
        @Override
        public long totalFreedBytes() { return 0; }
        
        @Override
        public long allocationRate() { return 0; }
        
        @Override
        public long promotionRate() { return 0; }
        
        @Override
        public long heapUsed() { return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); }
        
        @Override
        public long heapCapacity() { return Runtime.getRuntime().totalMemory(); }
        
        @Override
        public long heapMax() { return Runtime.getRuntime().maxMemory(); }
        
        @Override
        public long youngGenUsed() { return 0; }
        
        @Override
        public long youngGenCapacity() { return 0; }
        
        @Override
        public long oldGenUsed() { return 0; }
        
        @Override
        public long oldGenCapacity() { return 0; }
        
        @Override
        public long metaspaceUsed() { return 0; }
        
        @Override
        public long metaspaceCapacity() { return 0; }
        
        @Override
        public double gcOverhead() { return 0.0; }
        
        @Override
        public int gcThreads() { return 0; }
        
        @Override
        public int concurrentGcThreads() { return 0; }
        
        @Override
        public void reset() { /* no-op */ }
    }
}
