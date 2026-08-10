package jdk.aprismate;

import jdk.aprismate.runtime.Capability;

/**
 * VmInfo - VM build identity and AprismJDK capability descriptor.
 * 
 * <p>This class provides information about the runtime environment and
 * exposes which AprismJDK-specific capabilities are available. Mods should
 * query capabilities rather than assume them, enabling graceful degradation
 * on stock OpenJDK.
 * 
 * <p>Enhanced in v26.1-Alpha.1 with Capability API support.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.1
 */
public final class VmInfo {
    
    private static final Capability CAPABILITIES = buildCapabilities();
    
    private VmInfo() {
        // Utility class - no instantiation
    }
    
    private static Capability buildCapabilities() {
        String version = getAprismJdkVersion();
        if (version == null) {
            version = "stock-openjdk";
        }
        
        return Capability.builder(version)
            // v26.1-Alpha.1 capabilities
            .enable(Capability.AGENT_LOGGING)
            .enable(Capability.AGENT_INSTRUMENTATION)
            .enable(Capability.AGENT_FAILSAFE)
            // v26.0 capabilities
            .enable(Capability.RESOURCE_MANAGER)
            .enable(Capability.RESOURCE_LEAK_DETECTION)
            // Future capabilities (disabled until implemented)
            .disable(Capability.TRANSFORM_RETRANSFORM)
            .disable(Capability.TRANSFORM_REDEFINE)
            .disable(Capability.TRANSFORM_STRUCTURAL)
            .disable(Capability.HOOK_METHOD_ENTRY)
            .disable(Capability.HOOK_METHOD_EXIT)
            .disable(Capability.HOOK_JIT_SURVIVAL)
            .disable(Capability.INTROSPECT_THREADS)
            .disable(Capability.INTROSPECT_HEAP)
            .disable(Capability.INTROSPECT_JIT)
            .build();
    }
    
    /**
     * Returns the AprismJDK version string.
     * 
     * @return the version string (e.g., "v26.1-Alpha.1"), or null if running
     *         on stock OpenJDK
     */
    public static String getAprismJdkVersion() {
        // Check system property set by AprismJDK build
        return System.getProperty("aprismjdk.version");
    }
    
    /**
     * Returns the capability descriptor for this runtime.
     * 
     * <p>Use this to query which AprismJDK features are available:
     * <pre>{@code
     * Capability cap = VmInfo.getCapabilities();
     * if (cap.isEnabled(Capability.AGENT_LOGGING)) {
     *     // Use agent logging
     * }
     * }</pre>
     * 
     * @return the capability descriptor
     * @since 26.1-Alpha.1
     */
    public static Capability getCapabilities() {
        return CAPABILITIES;
    }
    
    /**
     * Checks if a specific capability is enabled.
     * 
     * @param capability the capability name
     * @return true if the capability is enabled
     * @since 26.1-Alpha.1
     */
    public static boolean isCapabilityEnabled(String capability) {
        return CAPABILITIES.isEnabled(capability);
    }
    
    /**
     * Checks if the current runtime is AprismJDK.
     * 
     * @return true if running on AprismJDK, false if stock OpenJDK
     */
    public static boolean isAprismJdk() {
        return getAprismJdkVersion() != null;
    }
    
    /**
     * Returns the upstream OpenJDK version this AprismJDK tracks.
     * 
     * @return the OpenJDK feature version (e.g., 25 for OpenJDK 25 LTS)
     */
    public static int getOpenJdkVersion() {
        String version = System.getProperty("java.version");
        if (version != null && !version.isEmpty()) {
            // Parse major version from version string
            int dotIndex = version.indexOf('.');
            if (dotIndex > 0) {
                try {
                    return Integer.parseInt(version.substring(0, dotIndex));
                } catch (NumberFormatException e) {
                    // Fall through
                }
            }
        }
        return Runtime.version().feature();
    }
    
    /**
     * Returns the VM vendor string.
     * 
     * @return the vendor string (e.g., "Aprism" for AprismJDK)
     */
    public static String getVendor() {
        return System.getProperty("java.vendor", "Unknown");
    }
    
    /**
     * Returns the VM name.
     * 
     * @return the VM name (e.g., "OpenJDK 64-Bit Server VM")
     */
    public static String getVmName() {
        return System.getProperty("java.vm.name", "Unknown");
    }
    
    /**
     * Returns the VM version.
     * 
     * @return the VM version string
     */
    public static String getVmVersion() {
        return System.getProperty("java.vm.version", "Unknown");
    }
    
    /**
     * Returns the build timestamp.
     * 
     * @return the build timestamp, or null if not available
     * @since v26.0-Alpha.2
     */
    public static String getBuildTimestamp() {
        return System.getProperty("aprismjdk.build.timestamp");
    }
    
    /**
     * Returns the build commit hash.
     * 
     * @return the git commit hash, or null if not available
     * @since v26.0-Alpha.2
     */
    public static String getBuildCommit() {
        return System.getProperty("aprismjdk.build.commit");
    }
    
    /**
     * Returns comprehensive build information.
     * 
     * @return a multi-line string with detailed build info
     * @since v26.0-Alpha.2
     */
    public static String getBuildInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("AprismJDK Build Information\n");
        sb.append("===========================\n");
        
        String aprismVersion = getAprismJdkVersion();
        if (aprismVersion != null) {
            sb.append("AprismJDK Version: ").append(aprismVersion).append("\n");
        } else {
            sb.append("Runtime: Stock OpenJDK (not AprismJDK)\n");
        }
        
        sb.append("OpenJDK Version: ").append(getOpenJdkVersion()).append("\n");
        sb.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        sb.append("Vendor: ").append(getVendor()).append("\n");
        sb.append("VM Name: ").append(getVmName()).append("\n");
        sb.append("VM Version: ").append(getVmVersion()).append("\n");
        
        String buildTime = getBuildTimestamp();
        if (buildTime != null) {
            sb.append("Build Time: ").append(buildTime).append("\n");
        }
        
        String commit = getBuildCommit();
        if (commit != null) {
            sb.append("Commit: ").append(commit).append("\n");
        }
        
        sb.append("\nCapabilities:\n");
        sb.append("  ClassRedefiner+: ").append(hasClassRedefinerPlus() ? "YES" : "NO").append("\n");
        sb.append("  MethodHookRegistry+: ").append(hasMethodHookRegistryPlus() ? "YES" : "NO").append("\n");
        sb.append("  BytecodeTransformer: ").append(hasBytecodeTransformer() ? "YES" : "NO").append("\n");
        sb.append("  VmIntrospection: ").append(hasVmIntrospection() ? "YES" : "NO").append("\n");
        
        return sb.toString();
    }
    
    /**
     * Checks if ClassRedefiner+ capability is available.
     * 
     * <p>ClassRedefiner+ allows structural class changes (add/remove fields/methods)
     * that stock Instrumentation.redefineClasses refuses.
     * 
     * @return true if available, false if running on stock JDK (graceful degradation)
     * @since v26.0-Alpha.1 (stub), v26.1-Alpha.2 (implementation)
     */
    public static boolean hasClassRedefinerPlus() {
        // Stub: always false until v26.1-Alpha.2
        return false;
    }
    
    /**
     * Checks if MethodHookRegistry+ capability is available.
     * 
     * <p>MethodHookRegistry+ allows JIT-safe hooks that survive inlining.
     * 
     * @return true if available, false if running on stock JDK
     * @since v26.0-Alpha.1 (stub), v26.1-Alpha.4 (implementation)
     */
    public static boolean hasMethodHookRegistryPlus() {
        // Stub: always false until v26.1-Alpha.4
        return false;
    }
    
    /**
     * Checks if BytecodeTransformer capability is available.
     * 
     * <p>BytecodeTransformer provides load-time ASM-based transformation.
     * 
     * @return true if available, false if running on stock JDK
     * @since v26.0-Alpha.1 (stub), v26.1-Alpha.5 (implementation)
     */
    public static boolean hasBytecodeTransformer() {
        // Stub: always false until v26.1-Alpha.5
        return false;
    }
    
    /**
     * Checks if VmIntrospection APIs are available.
     * 
     * <p>VmIntrospection includes ThreadInsight, HeapInsight, JitInsight.
     * 
     * @return true if available, false if running on stock JDK
     * @since v26.0-Alpha.1 (stub), v26.1-Alpha.6 (implementation)
     */
    public static boolean hasVmIntrospection() {
        // Stub: always false until v26.1-Alpha.6
        return false;
    }
}
