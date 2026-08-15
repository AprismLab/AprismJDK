package jdk.aprismate.profiler;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

/**
 * Factory for Profiler instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class ProfilerFactory {
    
    private ProfilerFactory() {
        // No instantiation
    }
    
    static Profiler.CpuProfiler cpu() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (Profiler.CpuProfiler) implClass.getMethod("cpu").invoke(null);
        } catch (Exception e) {
            return new StubCpuProfiler();
        }
    }
    
    static Profiler.AllocationProfiler allocation() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (Profiler.AllocationProfiler) implClass.getMethod("allocation").invoke(null);
        } catch (Exception e) {
            return new StubAllocationProfiler();
        }
    }
    
    static Profiler.WallClockProfiler wallClock() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (Profiler.WallClockProfiler) implClass.getMethod("wallClock").invoke(null);
        } catch (Exception e) {
            return new StubWallClockProfiler();
        }
    }
    
    static Profiler.LockProfiler lock() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (Profiler.LockProfiler) implClass.getMethod("lock").invoke(null);
        } catch (Exception e) {
            return new StubLockProfiler();
        }
    }
    
    static ProfileResult stop() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (ProfileResult) implClass.getMethod("stop").invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("No profiling session active or requires AprismJDK");
        }
    }
    
    static boolean isActive() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (boolean) implClass.getMethod("isActive").invoke(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    static Profiler.ProfilingMode mode() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.profiler.ProfilerImpl");
            return (Profiler.ProfilingMode) implClass.getMethod("mode").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Stub CpuProfiler implementation.
     */
    private static class StubCpuProfiler implements Profiler.CpuProfiler {
        
        @Override
        public Profiler.CpuProfiler interval(Duration interval) {
            return this;
        }
        
        @Override
        public Profiler.CpuProfiler includeNative() {
            return this;
        }
        
        @Override
        public Profiler.CpuProfiler includeKernel() {
            return this;
        }
        
        @Override
        public Profiler.CpuProfiler threads(Predicate<Thread> threadFilter) {
            return this;
        }
        
        @Override
        public void start() {
            throw new UnsupportedOperationException(
                "CPU profiling requires AprismJDK. Running on stock JDK.");
        }
    }
    
    /**
     * Stub AllocationProfiler implementation.
     */
    private static class StubAllocationProfiler implements Profiler.AllocationProfiler {
        
        @Override
        public Profiler.AllocationProfiler threshold(long bytes) {
            return this;
        }
        
        @Override
        public Profiler.AllocationProfiler classes(Predicate<Class<?>> classFilter) {
            return this;
        }
        
        @Override
        public Profiler.AllocationProfiler includeSize() {
            return this;
        }
        
        @Override
        public void start() {
            throw new UnsupportedOperationException(
                "Allocation profiling requires AprismJDK. Running on stock JDK.");
        }
    }
    
    /**
     * Stub WallClockProfiler implementation.
     */
    private static class StubWallClockProfiler implements Profiler.WallClockProfiler {
        
        @Override
        public Profiler.WallClockProfiler interval(Duration interval) {
            return this;
        }
        
        @Override
        public Profiler.WallClockProfiler threads(Predicate<Thread> threadFilter) {
            return this;
        }
        
        @Override
        public void start() {
            throw new UnsupportedOperationException(
                "Wall-clock profiling requires AprismJDK. Running on stock JDK.");
        }
    }
    
    /**
     * Stub LockProfiler implementation.
     */
    private static class StubLockProfiler implements Profiler.LockProfiler {
        
        @Override
        public Profiler.LockProfiler threshold(Duration threshold) {
            return this;
        }
        
        @Override
        public Profiler.LockProfiler monitors(Predicate<Object> monitorFilter) {
            return this;
        }
        
        @Override
        public void start() {
            throw new UnsupportedOperationException(
                "Lock profiling requires AprismJDK. Running on stock JDK.");
        }
    }
}
