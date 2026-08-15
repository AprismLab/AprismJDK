package jdk.aprismate.jit;

import java.lang.annotation.*;

/**
 * CompileImmediately - Annotation to force immediate JIT compilation.
 * 
 * <p>Methods annotated with this will be compiled to native code
 * immediately on first use, rather than waiting to become hot.
 * 
 * <p>Use this for critical paths that should always be optimized:
 * <pre>{@code
 * @CompileImmediately
 * public int criticalMethod() {
 *     // This will be compiled immediately
 *     return computeResult();
 * }
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CompileImmediately {
    
    /**
     * The target compilation level.
     * 
     * <p>Defaults to TIER_4 (C2, aggressive optimization).
     * 
     * @return compilation level
     */
    JitCompiler.CompilationLevel level() default JitCompiler.CompilationLevel.TIER_4;
}
