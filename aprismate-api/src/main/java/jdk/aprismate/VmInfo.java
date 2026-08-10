package jdk.aprismate;

/**
 * VmInfo - VM build identity and AprismJDK capability descriptor.
 * 
 * <p>This class provides information about the runtime environment and
 * exposes which AprismJDK-specific capabilities are available. Mods should
 * query capabilities rather than assume them, enabling graceful degradation
 * on stock OpenJDK.
 * 
 * <p>This is the v26.0-Alpha.1 stub implementation. Full capability reporting
 * will be delivered in v26.0-Alpha.6.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.1
 */
public final class VmInfo {
    
    private VmInfo() {
        // Utility class - no instantiation
    }
    
    /**
     * Returns the AprismJDK version string.
     * 
     * @return the version string (e.g., "v26.0-Alpha.1"), or null if running
     *         on stock OpenJDK
     */
    public static String getAprismJdkVersion() {
        // Check system property set by AprismJDK build
        return System.getProperty("aprismjdk.version");
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
     * @return the vendor string (e.g., "AprismLab" for AprismJDK)
     */
    public static String getVendor() {
        return System.getProperty("java.vendor", "Unknown");
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
