package jdk.aprismate.test.compatibility;

import jdk.aprismate.VmInfo;
import jdk.aprismate.util.JavaVersion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that all APIs provide safe fallback behavior on Stock JDK.
 * <p>
 * These tests verify that the API returns sensible defaults when the
 * AprismJDK implementation is not available.
 */
class StockJdkFallbackTest {
    
    @Test
    void testVmInfoFallback() {
        // Should return valid data even on Stock JDK
        assertNotNull(VmInfo.getVmVersion());
        assertNotNull(VmInfo.getVendor());
        assertNotNull(VmInfo.getVmName());
        
        // On Stock JDK, isAprismJdk should return false
        if (JavaVersion.isStockJDK()) {
            assertFalse(VmInfo.isAprismJdk());
            assertNull(VmInfo.getAprismJdkVersion());
        }
    }
    
    @Test
    void testAllApisNeverReturnNull() {
        // Critical: VmInfo methods should never return null
        assertNotNull(VmInfo.getVmVersion());
        assertNotNull(VmInfo.getVendor());
        assertNotNull(VmInfo.getVmName());
    }
    
    @Test
    void testStockJdkGracefulDegradation() {
        if (JavaVersion.isStockJDK()) {
            // On Stock JDK, APIs should degrade gracefully
            assertFalse(VmInfo.isAprismJdk());
            assertNull(VmInfo.getAprismJdkVersion());
            
            // Capabilities should be disabled
            assertFalse(VmInfo.hasClassRedefinerPlus());
            assertFalse(VmInfo.hasMethodHookRegistryPlus());
            assertFalse(VmInfo.hasBytecodeTransformer());
            assertFalse(VmInfo.hasVmIntrospection());
        }
    }
    
    @Test
    void testCapabilitiesOnStockJdk() {
        // Even on Stock JDK, capability queries should work
        assertDoesNotThrow(() -> {
            VmInfo.isCapabilityEnabled("AGENT_LOGGING");
            VmInfo.getCapabilities();
        });
    }
    
    @Test
    void testBuildInfoOnStockJdk() {
        // Build info methods should not throw on Stock JDK
        assertDoesNotThrow(() -> {
            String info = VmInfo.getBuildInfo();
            assertNotNull(info);
            assertTrue(info.contains("Version") || info.contains("Runtime"));
        });
    }
}
