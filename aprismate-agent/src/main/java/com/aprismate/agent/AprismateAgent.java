package com.aprismate.agent;

import java.lang.instrument.Instrumentation;

/**
 * AprismateAgent - The flagship component of AprismJDK.
 * 
 * <p>A JavaAgent-like agent bundled with the AprismJDK runtime, providing
 * deep VM integration for the Aprism loader ecosystem.
 * 
 * <p>This is the v26.0-Alpha.1 skeleton implementation. Full capabilities
 * (ClassRedefiner+, MethodHookRegistry+, BytecodeTransformer) will be
 * delivered in the v26.1 line.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.1
 */
public class AprismateAgent {
    
    private static Instrumentation instrumentation;
    private static volatile boolean initialized = false;
    
    /**
     * Premain entry point - invoked before the application's main method.
     * 
     * @param agentArgs agent arguments (semicolon-separated key=value pairs)
     * @param inst the instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        if (initialized) {
            System.err.println("[AprismateAgent] WARNING: Agent already initialized, ignoring premain");
            return;
        }
        
        instrumentation = inst;
        initialized = true;
        
        System.out.println("[AprismateAgent] v26.0-Alpha.1 attached via premain");
        System.out.println("[AprismateAgent] Can redefine classes: " + inst.isRedefineClassesSupported());
        System.out.println("[AprismateAgent] Can retransform classes: " + inst.isRetransformClassesSupported());
        
        // Parse agent arguments if present
        if (agentArgs != null && !agentArgs.isEmpty()) {
            parseArguments(agentArgs);
        }
    }
    
    /**
     * Agentmain entry point - invoked when attached to a running JVM.
     * 
     * @param agentArgs agent arguments
     * @param inst the instrumentation instance
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        if (initialized) {
            System.err.println("[AprismateAgent] WARNING: Agent already initialized, ignoring agentmain");
            return;
        }
        
        instrumentation = inst;
        initialized = true;
        
        System.out.println("[AprismateAgent] v26.0-Alpha.1 attached via agentmain (hot-attach)");
        System.out.println("[AprismateAgent] Can redefine classes: " + inst.isRedefineClassesSupported());
        System.out.println("[AprismateAgent] Can retransform classes: " + inst.isRetransformClassesSupported());
        
        if (agentArgs != null && !agentArgs.isEmpty()) {
            parseArguments(agentArgs);
        }
    }
    
    /**
     * Returns the Instrumentation instance provided by the JVM.
     * 
     * @return the instrumentation instance, or null if not initialized
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
    
    /**
     * Checks if the agent has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    private static void parseArguments(String agentArgs) {
        System.out.println("[AprismateAgent] Arguments: " + agentArgs);
        // Argument parsing will be implemented in later alphas
    }
}
