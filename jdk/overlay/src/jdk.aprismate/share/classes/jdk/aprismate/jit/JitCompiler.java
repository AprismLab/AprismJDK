package jdk.aprismate.jit;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * JitCompiler - Advanced JIT (Just-In-Time) compiler control and introspection.
 * 
 * <p>This API provides fine-grained control over the JIT compiler, enabling:
 * <ul>
 *   <li>Profiling-guided optimization (PGO)</li>
 *   <li>Tiered compilation control</li>
 *   <li>Deoptimization and recompilation</li>
 *   <li>Compilation statistics and introspection</li>
 * </ul>
 * 
 * <h2>Compilation Tiers</h2>
 * <p>Modern JVMs use tiered compilation with multiple optimization levels:
 * <pre>
 * Tier 0: Interpreter (no compilation)
 * Tier 1: C1 compiler - simple compilation (fast compile, moderate performance)
 * Tier 2: C1 compiler - limited profiling
 * Tier 3: C1 compiler - full profiling
 * Tier 4: C2 compiler - aggressive optimization (slow compile, best performance)
 * </pre>
 * 
 * <h2>Usage Example - Force Compilation</h2>
 * <pre>{@code
 * public class HotPath {
 *     
 *     @CompileImmediately
 *     public static int criticalMethod(int x) {
 *         return x * x + 2 * x + 1;
 *     }
 *     
 *     public static void main(String[] args) {
 *         // Force compilation to tier 4 (C2)
 *         JitCompiler.compileMethod(
 *             HotPath.class, "criticalMethod", 
 *             CompilationLevel.TIER_4
 *         );
 *         
 *         // Verify compilation
 *         CompilationInfo info = JitCompiler.getCompilationInfo(
 *             HotPath.class, "criticalMethod"
 *         );
 *         System.out.println("Compiled: " + info.isCompiled());
 *         System.out.println("Level: " + info.level());
 *         System.out.println("Native code size: " + info.nativeCodeSize() + " bytes");
 *     }
 * }
 * }</pre>
 * 
 * <h2>Usage Example - Profile-Guided Optimization</h2>
 * <pre>{@code
 * // Collect profiles during training run
 * JitCompiler.enableProfiling();
 * runTrainingWorkload();
 * ProfileData profiles = JitCompiler.collectProfiles();
 * profiles.save(Path.of("profiles.dat"));
 * 
 * // Use profiles in production
 * ProfileData profiles = ProfileData.load(Path.of("profiles.dat"));
 * JitCompiler.applyProfiles(profiles);
 * runProductionWorkload();  // 10-30% faster
 * }</pre>
 * 
 * <h2>Usage Example - Deoptimization Detection</h2>
 * <pre>{@code
 * JitCompiler.addDeoptimizationListener((method, reason) -> {
 *     System.err.println("Deoptimization: " + method + " - " + reason);
 *     // Reason might be: "uncommon trap", "not entrant", etc.
 * });
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface JitCompiler {
    
    /**
     * Compiles a method to the specified level.
     * 
     * <p>This forces immediate compilation without waiting for the method
     * to become hot. Useful for critical paths that should always be optimized.
     * 
     * @param clazz the class containing the method
     * @param methodName the method name
     * @param level the target compilation level
     * @return true if compilation was successful
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if method is not found
     */
    static boolean compileMethod(Class<?> clazz, String methodName, CompilationLevel level) {
        return JitCompilerFactory.compileMethod(clazz, methodName, level);
    }
    
    /**
     * Compiles a specific method overload.
     * 
     * @param clazz the class containing the method
     * @param methodName the method name
     * @param parameterTypes the parameter types
     * @param level the target compilation level
     * @return true if compilation was successful
     * @throws NullPointerException if any parameter is null
     * @throws NoSuchMethodException if method is not found
     */
    static boolean compileMethod(Class<?> clazz, String methodName, 
                                 Class<?>[] parameterTypes, CompilationLevel level) 
            throws NoSuchMethodException {
        return JitCompilerFactory.compileMethod(clazz, methodName, parameterTypes, level);
    }
    
    /**
     * Compiles a method using reflection.
     * 
     * @param method the method to compile
     * @param level the target compilation level
     * @return true if compilation was successful
     * @throws NullPointerException if any parameter is null
     */
    static boolean compileMethod(Method method, CompilationLevel level) {
        return JitCompilerFactory.compileMethod(method, level);
    }
    
    /**
     * Decompiles a method, forcing it back to interpreter mode.
     * 
     * <p>Useful for methods that were speculatively optimized with
     * incorrect assumptions.
     * 
     * @param method the method to decompile
     */
    static void decompileMethod(Method method) {
        JitCompilerFactory.decompileMethod(method);
    }
    
    /**
     * Returns compilation information for a method.
     * 
     * @param clazz the class containing the method
     * @param methodName the method name
     * @return compilation info, or null if method not found
     * @throws NullPointerException if any parameter is null
     */
    static CompilationInfo getCompilationInfo(Class<?> clazz, String methodName) {
        return JitCompilerFactory.getCompilationInfo(clazz, methodName);
    }
    
    /**
     * Returns compilation information for a method.
     * 
     * @param method the method
     * @return compilation info
     * @throws NullPointerException if method is null
     */
    static CompilationInfo getCompilationInfo(Method method) {
        return JitCompilerFactory.getCompilationInfo(method);
    }
    
    /**
     * Returns all compiled methods.
     * 
     * @return set of compiled methods
     */
    static Set<Method> getCompiledMethods() {
        return JitCompilerFactory.getCompiledMethods();
    }
    
    /**
     * Enables profiling mode.
     * 
     * <p>In profiling mode, the JIT collects detailed execution statistics
     * that can be used for profile-guided optimization.
     */
    static void enableProfiling() {
        JitCompilerFactory.enableProfiling();
    }
    
    /**
     * Disables profiling mode.
     */
    static void disableProfiling() {
        JitCompilerFactory.disableProfiling();
    }
    
    /**
     * Collects current profile data.
     * 
     * <p>This captures branch frequencies, call sites, type profiles, etc.
     * 
     * @return profile data
     */
    static ProfileData collectProfiles() {
        return JitCompilerFactory.collectProfiles();
    }
    
    /**
     * Applies pre-collected profile data.
     * 
     * <p>The JIT compiler will use this data to guide optimization decisions.
     * 
     * @param profiles the profile data to apply
     * @throws NullPointerException if profiles is null
     */
    static void applyProfiles(ProfileData profiles) {
        JitCompilerFactory.applyProfiles(profiles);
    }
    
    /**
     * Adds a deoptimization listener.
     * 
     * <p>The listener is called whenever a method is deoptimized.
     * 
     * @param listener the listener to add
     * @throws NullPointerException if listener is null
     */
    static void addDeoptimizationListener(DeoptimizationListener listener) {
        JitCompilerFactory.addDeoptimizationListener(listener);
    }
    
    /**
     * Removes a deoptimization listener.
     * 
     * @param listener the listener to remove
     * @throws NullPointerException if listener is null
     */
    static void removeDeoptimizationListener(DeoptimizationListener listener) {
        JitCompilerFactory.removeDeoptimizationListener(listener);
    }
    
    /**
     * Returns global JIT compiler statistics.
     * 
     * @return compiler statistics
     */
    static CompilerStats getStats() {
        return JitCompilerFactory.getStats();
    }
    
    /**
     * Prints the assembly code for a compiled method.
     * 
     * <p>This requires the hsdis (HotSpot disassembler) library to be installed.
     * 
     * @param method the method
     * @return assembly code as a string, or null if not available
     * @throws NullPointerException if method is null
     */
    static String printAssembly(Method method) {
        return JitCompilerFactory.printAssembly(method);
    }
    
    /**
     * Compilation level enumeration.
     */
    enum CompilationLevel {
        /** Interpreter, no compilation. */
        TIER_0(0),
        
        /** C1 compiler - simple compilation. */
        TIER_1(1),
        
        /** C1 compiler - limited profiling. */
        TIER_2(2),
        
        /** C1 compiler - full profiling. */
        TIER_3(3),
        
        /** C2 compiler - aggressive optimization. */
        TIER_4(4);
        
        private final int level;
        
        CompilationLevel(int level) {
            this.level = level;
        }
        
        /**
         * Returns the numeric level.
         * 
         * @return level number (0-4)
         */
        public int level() {
            return level;
        }
        
        /**
         * Returns the level for a numeric value.
         * 
         * @param level the level number
         * @return the corresponding enum value
         * @throws IllegalArgumentException if level is invalid
         */
        public static CompilationLevel fromLevel(int level) {
            for (CompilationLevel cl : values()) {
                if (cl.level == level) {
                    return cl;
                }
            }
            throw new IllegalArgumentException("Invalid level: " + level);
        }
    }
}
