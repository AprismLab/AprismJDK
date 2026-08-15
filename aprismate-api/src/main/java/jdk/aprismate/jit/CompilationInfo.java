package jdk.aprismate.jit;

import java.lang.reflect.Method;

/**
 * CompilationInfo - Information about a compiled method.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface CompilationInfo {
    
    /**
     * Returns the compiled method.
     * 
     * @return the method
     */
    Method method();
    
    /**
     * Checks if the method is currently compiled.
     * 
     * @return true if compiled
     */
    boolean isCompiled();
    
    /**
     * Returns the current compilation level.
     * 
     * @return compilation level, or null if not compiled
     */
    JitCompiler.CompilationLevel level();
    
    /**
     * Returns the size of the generated native code in bytes.
     * 
     * @return native code size, or 0 if not compiled
     */
    long nativeCodeSize();
    
    /**
     * Returns the number of times this method has been invoked.
     * 
     * @return invocation count
     */
    long invocationCount();
    
    /**
     * Returns the number of times this method's loop bodies have been executed.
     * 
     * @return backedge count
     */
    long backedgeCount();
    
    /**
     * Returns the number of times this method has been compiled.
     * 
     * @return compilation count
     */
    int compilationCount();
    
    /**
     * Returns the number of times this method has been deoptimized.
     * 
     * @return deoptimization count
     */
    int deoptimizationCount();
    
    /**
     * Returns the time spent compiling this method in milliseconds.
     * 
     * @return compilation time in ms
     */
    long compilationTime();
    
    /**
     * Checks if this method has been marked as hot.
     * 
     * <p>Hot methods are candidates for compilation.
     * 
     * @return true if hot
     */
    boolean isHot();
    
    /**
     * Checks if this method is on-stack-replacement (OSR) compiled.
     * 
     * <p>OSR compilation happens when a long-running loop is detected
     * and compiled while the method is still executing.
     * 
     * @return true if OSR compiled
     */
    boolean isOsrCompiled();
    
    /**
     * Returns the entry address of the compiled code.
     * 
     * @return native code entry address, or 0 if not compiled
     */
    long entryPoint();
}
