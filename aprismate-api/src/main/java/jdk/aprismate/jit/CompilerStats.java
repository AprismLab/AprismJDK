package jdk.aprismate.jit;

/**
 * CompilerStats - Global JIT compiler statistics.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface CompilerStats {
    
    /**
     * Returns the total number of compiled methods.
     * 
     * @return total compilations
     */
    long totalCompilations();
    
    /**
     * Returns the number of methods compiled at each tier.
     * 
     * @param level the compilation level
     * @return compilation count for the tier
     */
    long compilationsAtLevel(JitCompiler.CompilationLevel level);
    
    /**
     * Returns the total number of deoptimizations.
     * 
     * @return total deoptimizations
     */
    long totalDeoptimizations();
    
    /**
     * Returns the number of currently compiled methods.
     * 
     * @return active compilation count
     */
    int activeCompiledMethods();
    
    /**
     * Returns the total time spent compiling in milliseconds.
     * 
     * @return total compilation time in ms
     */
    long totalCompilationTime();
    
    /**
     * Returns the total size of generated native code in bytes.
     * 
     * @return total native code size
     */
    long totalNativeCodeSize();
    
    /**
     * Returns the number of OSR (on-stack-replacement) compilations.
     * 
     * @return OSR compilation count
     */
    long osrCompilations();
    
    /**
     * Returns the number of standard (non-OSR) compilations.
     * 
     * @return standard compilation count
     */
    long standardCompilations();
    
    /**
     * Returns the number of failed compilations.
     * 
     * @return failed compilation count
     */
    long failedCompilations();
    
    /**
     * Returns the number of invalidated compilations.
     * 
     * @return invalidated compilation count
     */
    long invalidatedCompilations();
    
    /**
     * Calculates the average compilation time in milliseconds.
     * 
     * @return average compilation time in ms
     */
    default double averageCompilationTime() {
        long total = totalCompilations();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalCompilationTime() / total;
    }
    
    /**
     * Calculates the average native code size in bytes.
     * 
     * @return average code size
     */
    default long averageNativeCodeSize() {
        int active = activeCompiledMethods();
        if (active == 0) {
            return 0;
        }
        return totalNativeCodeSize() / active;
    }
    
    /**
     * Calculates the compilation success rate.
     * 
     * @return success rate percentage (0-100)
     */
    default double successRate() {
        long total = totalCompilations();
        if (total == 0) {
            return 100.0;
        }
        long successful = total - failedCompilations();
        return (successful * 100.0) / total;
    }
    
    /**
     * Resets all statistics to zero.
     */
    void reset();
}
