package jdk.aprismate.agent;

import java.lang.instrument.Instrumentation;

/**
 * Provides access to the {@link Instrumentation} instance for agent operations.
 * This context is initialized by the agent premain/agentmain method and provides
 * instrumentation capabilities to the rest of the agent framework.
 * 
 * <p>The instrumentation instance allows:
 * <ul>
 *   <li>Class retransformation and redefinition</li>
 *   <li>Adding transformers to modify bytecode at load time</li>
 *   <li>Querying loaded classes</li>
 *   <li>Getting object sizes</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * Instrumentation inst = InstrumentationContext.getInstrumentation();
 * if (inst != null && inst.isRetransformClassesSupported()) {
 *     inst.addTransformer(myTransformer, true);
 * }
 * }</pre>
 * 
 * @since 26.1-Alpha.1
 */
public final class InstrumentationContext {
    private static volatile Instrumentation instrumentation;
    
    private InstrumentationContext() {
        // Prevent instantiation
    }
    
    /**
     * Initializes the instrumentation context. This method should only be called
     * by the agent premain/agentmain method.
     * 
     * @param inst the instrumentation instance provided by the JVM
     * @throws IllegalStateException if already initialized
     */
    public static void initialize(Instrumentation inst) {
        if (instrumentation != null) {
            throw new IllegalStateException("InstrumentationContext already initialized");
        }
        instrumentation = inst;
        AgentLogger.getInstance().info("InstrumentationContext initialized");
    }
    
    /**
     * Returns the instrumentation instance, or null if not initialized.
     * 
     * @return the instrumentation instance, or null
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
    
    /**
     * Checks if instrumentation is available.
     * 
     * @return true if instrumentation is available
     */
    public static boolean isAvailable() {
        return instrumentation != null;
    }
    
    /**
     * Checks if class retransformation is supported.
     * 
     * @return true if retransformation is supported
     */
    public static boolean isRetransformSupported() {
        return instrumentation != null && instrumentation.isRetransformClassesSupported();
    }
    
    /**
     * Checks if class redefinition is supported.
     * 
     * @return true if redefinition is supported
     */
    public static boolean isRedefineSupported() {
        return instrumentation != null && instrumentation.isRedefineClassesSupported();
    }
    
    /**
     * Checks if native method prefix is supported.
     * 
     * @return true if native method prefix is supported
     */
    public static boolean isNativeMethodPrefixSupported() {
        return instrumentation != null && instrumentation.isNativeMethodPrefixSupported();
    }
}
