package jdk.aprismate.util;

/**
 * Java version detection utility.
 * <p>
 * Provides methods to check the running Java version for conditional feature support.
 * 
 * @since 26.1-Alpha.8
 */
public final class JavaVersion {
    
    private static final int FEATURE_VERSION = Runtime.version().feature();
    private static final String VERSION_STRING = System.getProperty("java.version");
    private static final String VENDOR = System.getProperty("java.vendor");
    
    private JavaVersion() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Checks if running on Java 17 or higher.
     * 
     * @return true if Java 17+, false otherwise
     */
    public static boolean isJava17OrHigher() {
        return FEATURE_VERSION >= 17;
    }
    
    /**
     * Checks if running on Java 21 or higher.
     * 
     * @return true if Java 21+, false otherwise
     */
    public static boolean isJava21OrHigher() {
        return FEATURE_VERSION >= 21;
    }
    
    /**
     * Checks if running on Java 25 or higher.
     * 
     * @return true if Java 25+, false otherwise
     */
    public static boolean isJava25OrHigher() {
        return FEATURE_VERSION >= 25;
    }
    
    /**
     * Gets the Java feature version number.
     * <p>
     * For example, returns 21 for Java 21.0.1.
     * 
     * @return feature version number
     */
    public static int featureVersion() {
        return FEATURE_VERSION;
    }
    
    /**
     * Gets the full Java version string.
     * <p>
     * For example, "21.0.1" or "17.0.9".
     * 
     * @return version string
     */
    public static String versionString() {
        return VERSION_STRING;
    }
    
    /**
     * Gets the Java vendor name.
     * <p>
     * For example, "Oracle Corporation", "Aprism Project", etc.
     * 
     * @return vendor name
     */
    public static String vendor() {
        return VENDOR;
    }
    
    /**
     * Checks if running on AprismJDK.
     * 
     * @return true if AprismJDK, false otherwise
     */
    public static boolean isAprismJDK() {
        return VENDOR != null && VENDOR.contains("Aprism");
    }
    
    /**
     * Checks if running on Stock (standard) JDK.
     * 
     * @return true if not AprismJDK, false if AprismJDK
     */
    public static boolean isStockJDK() {
        return !isAprismJDK();
    }
}
