package jdk.aprismate.runtime;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Provides introspection into JIT (Just-In-Time) compiler state and compilation activity.
 * <p>
 * This API exposes information about method compilation status, compilation levels,
 * and allows forcing deoptimization for testing purposes.
 * </p>
 *
 * @since v26.1-Alpha.7
 */
public interface JitInsight {
    
    /**
     * Represents a method in the compilation queue.
     */
    interface CompilationTask {
        /**
         * Returns the method being compiled.
         *
         * @return the method
         */
        Method getMethod();
        
        /**
         * Returns the target compilation level.
         *
         * @return the compilation level
         */
        CompilationLevel getLevel();
        
        /**
         * Returns the priority of this compilation task.
         * Higher values indicate higher priority.
         *
         * @return task priority
         */
        int getPriority();
        
        /**
         * Returns whether this is a blocking compilation.
         *
         * @return true if blocking
         */
        boolean isBlocking();
    }
    
    /**
     * Represents a compiled method.
     */
    interface CompiledMethod {
        /**
         * Returns the compiled method.
         *
         * @return the method
         */
        Method getMethod();
        
        /**
         * Returns the compilation level of this method.
         *
         * @return the compilation level
         */
        CompilationLevel getLevel();
        
        /**
         * Returns the size of the compiled code in bytes.
         *
         * @return code size, or -1 if unavailable
         */
        int getCodeSize();
        
        /**
         * Returns the number of times this method has been invoked.
         *
         * @return invocation count, or -1 if unavailable
         */
        long getInvocationCount();
        
        /**
         * Returns whether this method is currently inlined.
         *
         * @return true if inlined
         */
        boolean isInlined();
    }
    
    /**
     * JIT compilation levels in HotSpot tiered compilation.
     */
    enum CompilationLevel {
        /** No compilation - interpreter only */
        INTERPRETED(0),
        
        /** C1 compilation with no profiling */
        C1_NO_PROFILING(1),
        
        /** C1 compilation with invocation and backedge counters */
        C1_LIMITED_PROFILING(2),
        
        /** C1 compilation with full profiling */
        C1_FULL_PROFILING(3),
        
        /** C2 optimizing compiler */
        C2(4),
        
        /** Unknown or custom level */
        UNKNOWN(-1);
        
        private final int level;
        
        CompilationLevel(int level) {
            this.level = level;
        }
        
        /**
         * Returns the numeric level.
         *
         * @return the level number
         */
        public int getLevel() {
            return level;
        }
        
        /**
         * Returns the compilation level for a numeric value.
         *
         * @param level the numeric level
         * @return the corresponding enum constant
         */
        public static CompilationLevel fromLevel(int level) {
            for (CompilationLevel cl : values()) {
                if (cl.level == level) {
                    return cl;
                }
            }
            return UNKNOWN;
        }
    }
    
    /**
     * Returns the current compilation queue.
     * <p>
     * This includes methods waiting to be compiled by the JIT compiler.
     * The list is a snapshot and may change immediately after this call.
     * </p>
     *
     * @return an immutable list of compilation tasks
     */
    List<CompilationTask> getCompilationQueue();
    
    /**
     * Returns all currently compiled methods.
     * <p>
     * This includes methods compiled by C1 and C2 compilers.
     * The list is a snapshot and may not include very recent compilations.
     * </p>
     *
     * @return an immutable list of compiled methods
     */
    List<CompiledMethod> getCompiledMethods();
    
    /**
     * Returns the compilation level of a specific method.
     * <p>
     * If the method has not been compiled, returns {@link CompilationLevel#INTERPRETED}.
     * If the method has been compiled at multiple levels, returns the highest level.
     * </p>
     *
     * @param method the method to query
     * @return the compilation level
     * @throws NullPointerException if method is null
     */
    CompilationLevel getMethodCompilationLevel(Method method);
    
    /**
     * Forces deoptimization of a compiled method.
     * <p>
     * This causes the method to revert to interpreted execution. The JIT compiler
     * may recompile the method later based on hotness counters.
     * </p>
     * <p>
     * <strong>Warning:</strong> This operation is expensive and should only be used
     * for testing or debugging purposes. It is not supported on all JVMs.
     * </p>
     *
     * @param method the method to deoptimize
     * @return true if deoptimization succeeded, false if not supported or method not compiled
     * @throws NullPointerException if method is null
     */
    boolean deoptimizeMethod(Method method);
    
    /**
     * Returns whether the JIT compiler is currently active.
     * <p>
     * The compiler may be disabled via JVM flags or paused during certain operations.
     * </p>
     *
     * @return true if the compiler is active
     */
    boolean isCompilerActive();
    
    /**
     * Returns the total number of methods compiled since JVM startup.
     *
     * @return total compilation count, or -1 if unavailable
     */
    long getTotalCompilations();
    
    /**
     * Returns the total time spent in compilation (milliseconds).
     *
     * @return total compilation time in ms, or -1 if unavailable
     */
    long getTotalCompilationTime();
}
