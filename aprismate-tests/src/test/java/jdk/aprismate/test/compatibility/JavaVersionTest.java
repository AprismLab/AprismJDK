package jdk.aprismate.test.compatibility;

import jdk.aprismate.util.JavaVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JavaVersion utility.
 */
class JavaVersionTest {
    
    @Test
    void testFeatureVersion() {
        int version = JavaVersion.featureVersion();
        assertTrue(version >= 17, "AprismJDK requires Java 17 or higher");
    }
    
    @Test
    void testVersionChecks() {
        int version = JavaVersion.featureVersion();
        
        if (version >= 17) {
            assertTrue(JavaVersion.isJava17OrHigher());
        }
        
        if (version >= 21) {
            assertTrue(JavaVersion.isJava21OrHigher());
        } else {
            assertFalse(JavaVersion.isJava21OrHigher());
        }
        
        if (version >= 25) {
            assertTrue(JavaVersion.isJava25OrHigher());
        } else {
            assertFalse(JavaVersion.isJava25OrHigher());
        }
    }
    
    @Test
    void testVersionString() {
        String versionString = JavaVersion.versionString();
        assertNotNull(versionString);
        assertFalse(versionString.isEmpty());
    }
    
    @Test
    void testVendor() {
        String vendor = JavaVersion.vendor();
        assertNotNull(vendor);
        assertFalse(vendor.isEmpty());
    }
    
    @Test
    void testAprismJDKDetection() {
        boolean isAprism = JavaVersion.isAprismJDK();
        boolean isStock = JavaVersion.isStockJDK();
        
        // Should be mutually exclusive
        assertNotEquals(isAprism, isStock);
        
        String vendor = JavaVersion.vendor();
        if (vendor.contains("Aprism")) {
            assertTrue(isAprism);
            assertFalse(isStock);
        } else {
            assertFalse(isAprism);
            assertTrue(isStock);
        }
    }
}
