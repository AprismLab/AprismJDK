package com.aprismate.agent;

import aprism.agent.api.metrics.AgentMetrics;
import aprism.agent.hooks.DefaultMethodHookRegistry;
import aprism.agent.metrics.DefaultMetricRegistry;
import jdk.aprismate.agent.MethodHookRegistry;

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
    private static MethodHookRegistry methodHookRegistry;
    
    /**
     * Premain entry point - invoked before the application's main method.
     * 
     * @param agentArgs agent arguments (semicolon-separated key=value pairs)
     * @param inst the instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            doPremain(agentArgs, inst);
        } catch (Throwable t) {
            initialized = false;
            instrumentation = null;
            System.err.println("[AprismateAgent] FAIL-SAFE: premain failed, agent disabled, JVM continues");
            t.printStackTrace(System.err);
        }
    }

    private static void doPremain(String agentArgs, Instrumentation inst) {
        if (initialized) {
            System.err.println("[AprismateAgent] WARNING: Agent already initialized, ignoring premain");
            return;
        }

        instrumentation = inst;
        initialized = true;

        // Initialize experiment framework (fail-safe: no-op if unavailable)
        try {
            aprism.agent.experiment.SafeExperiment.init(inst);
            aprism.agent.reload.HotReloader.init(inst);
        } catch (Throwable ignored) { }

        // Install startup profiler if enabled
        String startupProfile = System.getProperty("aprismate.startup.profile");
        if (startupProfile != null) {
            try {
                aprism.agent.startup.StartupProfiler.install(inst);
                System.out.println("[AprismateAgent] startup profiler installed");
            } catch (Throwable t) {
                System.err.println("[AprismateAgent] FAIL-SAFE: startup profiler failed, continuing");
            }
        }

        // Initialize metrics system
        initializeMetrics();

        // Initialize method hook registry
        initializeMethodHookRegistry();

        installPreOptimizer();

        logGcProfileAdvice();

        // Start diagnostic HTTP server if enabled
        aprism.agent.diag.DiagnosticServer.tryStart();

        System.out.println("[AprismateAgent] attached via premain");
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
        try {
            doAgentmain(agentArgs, inst);
        } catch (Throwable t) {
            initialized = false;
            instrumentation = null;
            System.err.println("[AprismateAgent] FAIL-SAFE: agentmain failed, agent disabled, target continues");
            t.printStackTrace(System.err);
        }
    }

    private static void doAgentmain(String agentArgs, Instrumentation inst) {
        if (initialized) {
            System.err.println("[AprismateAgent] WARNING: Agent already initialized, ignoring agentmain");
            return;
        }

        instrumentation = inst;
        initialized = true;

        // Initialize experiment framework (fail-safe: no-op if unavailable)
        try {
            aprism.agent.experiment.SafeExperiment.init(inst);
        } catch (Throwable ignored) { }

        // Initialize metrics system
        initializeMetrics();

        // Initialize method hook registry
        initializeMethodHookRegistry();

        installPreOptimizer();

        System.out.println("[AprismateAgent] attached via agentmain (hot-attach)");
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
     * Returns the MethodHookRegistry instance for registering method hooks.
     * 
     * @return the MethodHookRegistry instance, or null if not initialized
     * @since v26.1-Alpha.4
     */
    public static MethodHookRegistry getMethodHookRegistry() {
        return methodHookRegistry;
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
    
    /**
     * Logs GC profile advice if -Daprismate.gc.profile is set.
     */
    private static void logGcProfileAdvice() {
        String profile = System.getProperty("aprismate.gc.profile");
        if (profile == null || profile.isBlank()) {
            return;
        }
        try {
            var preset = jdk.aprismate.tuning.GcPresets.forName(profile);
            if (preset.isPresent()) {
                System.out.println("[AprismateAgent] GC profile '" + profile
                        + "': " + preset.get().asLaunchArgs());
            } else {
                System.out.println("[AprismateAgent] unknown GC profile '" + profile
                        + "'. Available:\n" + jdk.aprismate.tuning.GcPresets.describeAll());
            }
        } catch (Throwable ignored) { }
    }

    /**
     * Installs the pre-optimization transformer when explicitly enabled
     * via -Daprismate.optimizer.rules=<file>. Fail-safe: any setup error
     * logs and skips installation.
     */
    private static void installPreOptimizer() {
        String rules = System.getProperty("aprismate.optimizer.rules");
        if (rules == null || rules.isBlank()) {
            return;
        }
        try {
            var cfg = aprism.agent.optimize.OptimizerConfig.parse(java.nio.file.Path.of(rules));
            if (cfg.isEmpty()) {
                System.out.println("[AprismateAgent] optimizer: empty rules, skipped");
                return;
            }
            var transformer = new aprism.agent.optimize.PreOptimizationTransformer(cfg);
            instrumentation.addTransformer(transformer);
            System.out.println("[AprismateAgent] optimizer installed ("
                    + cfg.elisions().size() + " elisions, "
                    + cfg.probes().size() + " probes)");
        } catch (Throwable t) {
            System.err.println("[AprismateAgent] FAIL-SAFE: optimizer setup failed, continuing without it");
            t.printStackTrace(System.err);
        }
    }

    /**
     * Initializes the metrics system with a default registry.
     */
    private static void initializeMetrics() {
        try {
            AgentMetrics.setRegistry(new DefaultMetricRegistry());
            System.out.println("[AprismateAgent] Metrics system initialized");
            
            // Record agent initialization metric
            AgentMetrics.counter("aprism.agent.initialized", 1.0, 
                "version", "v26.1-Alpha.4");
        } catch (Exception e) {
            System.err.println("[AprismateAgent] Failed to initialize metrics: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the method hook registry.
     */
    private static void initializeMethodHookRegistry() {
        try {
            methodHookRegistry = new DefaultMethodHookRegistry();
            System.out.println("[AprismateAgent] MethodHookRegistry initialized");
            
            // Record initialization metric
            AgentMetrics.counter("aprism.agent.hooks.initialized", 1.0);
        } catch (Exception e) {
            System.err.println("[AprismateAgent] Failed to initialize MethodHookRegistry: " + e.getMessage());
        }
    }
}
