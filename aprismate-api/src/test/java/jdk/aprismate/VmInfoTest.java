package jdk.aprismate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for VmInfo API.
 */
class VmInfoTest {
    
    @Test
    @DisplayName("getOpenJdkVersion should return valid version number")
    void testGetOpenJdkVersion() {
        int version = VmInfo.getOpenJdkVersion();
        assertTrue(version >= 17, "OpenJDK version should be at least 17");
        assertTrue(version <= 30, "OpenJDK version should be reasonable");
    }
    
    @Test
    @DisplayName("getVendor should return non-null vendor string")
    void testGetVendor() {
        String vendor = VmInfo.getVendor();
        assertNotNull(vendor, "Vendor should not be null");
        assertFalse(vendor.isEmpty(), "Vendor should not be empty");
    }
    
    @Test
    @DisplayName("getVmName should return non-null VM name")
    void testGetVmName() {
        String vmName = VmInfo.getVmName();
        assertNotNull(vmName, "VM name should not be null");
        assertFalse(vmName.isEmpty(), "VM name should not be empty");
    }
    
    @Test
    @DisplayName("getVmVersion should return non-null VM version")
    void testGetVmVersion() {
        String vmVersion = VmInfo.getVmVersion();
        assertNotNull(vmVersion, "VM version should not be null");
        assertFalse(vmVersion.isEmpty(), "VM version should not be empty");
    }
    
    @Test
    @DisplayName("isAprismJdk should match getAprismJdkVersion presence")
    void testIsAprismJdk() {
        boolean isAprism = VmInfo.isAprismJdk();
        String version = VmInfo.getAprismJdkVersion();
        
        if (isAprism) {
            assertNotNull(version, "If isAprismJdk is true, version should not be null");
        } else {
            assertNull(version, "If isAprismJdk is false, version should be null");
        }
    }
    
    @Test
    @DisplayName("Capability checks should not throw exceptions")
    void testCapabilityChecks() {
        assertDoesNotThrow(() -> {
            VmInfo.hasClassRedefinerPlus();
            VmInfo.hasMethodHookRegistryPlus();
            VmInfo.hasBytecodeTransformer();
            VmInfo.hasVmIntrospection();
        }, "Capability checks should not throw exceptions");
    }
    
    @Test
    @DisplayName("All capabilities should be false in Alpha.2")
    void testCapabilitiesAreStubbed() {
        assertFalse(VmInfo.hasClassRedefinerPlus(), 
            "ClassRedefiner+ should be false until v26.1-Alpha.2");
        assertFalse(VmInfo.hasMethodHookRegistryPlus(), 
            "MethodHookRegistry+ should be false until v26.1-Alpha.4");
        assertFalse(VmInfo.hasBytecodeTransformer(), 
            "BytecodeTransformer should be false until v26.1-Alpha.5");
        assertFalse(VmInfo.hasVmIntrospection(), 
            "VmIntrospection should be false until v26.1-Alpha.6");
    }
    
    @Test
    @DisplayName("getBuildInfo should return non-null string")
    void testGetBuildInfo() {
        String buildInfo = VmInfo.getBuildInfo();
        assertNotNull(buildInfo, "Build info should not be null");
        assertFalse(buildInfo.isEmpty(), "Build info should not be empty");
        assertTrue(buildInfo.contains("Build Information"), 
            "Build info should contain header");
        assertTrue(buildInfo.contains("Capabilities"), 
            "Build info should contain capabilities section");
    }
    
    @Test
    @DisplayName("getBuildInfo should be printable")
    void testBuildInfoPrintable() {
        String buildInfo = VmInfo.getBuildInfo();
        // Just verify it can be printed without exceptions
        assertDoesNotThrow(() -> System.out.println(buildInfo));
    }
    
    @Test
    @DisplayName("Build metadata methods should not throw")
    void testBuildMetadata() {
        assertDoesNotThrow(() -> {
            VmInfo.getBuildTimestamp();
            VmInfo.getBuildCommit();
        }, "Build metadata methods should not throw exceptions");
    }
}
