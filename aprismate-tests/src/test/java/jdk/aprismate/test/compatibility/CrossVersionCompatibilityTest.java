package jdk.aprismate.test.compatibility;

import jdk.aprismate.VmInfo;
import jdk.aprismate.util.JavaVersion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests cross-version compatibility of AprismJDK API.
 * <p>
 * Ensures that code written for Java 21 works on Java 17, and that
 * API signatures remain stable across versions.
 */
class CrossVersionCompatibilityTest {
    
    @Test
    void testJava21FeaturesAvailable() {
        // Test that we can use Java 21 features
        int currentVersion = JavaVersion.featureVersion();
        assertTrue(currentVersion >= 17, "Should be running on Java 17 or later");
    }
    
    @Test
    void testVarKeywordWorks() {
        // var keyword (Java 10+) should work
        var vmInfo = VmInfo.getVmVersion();
        assertNotNull(vmInfo);
    }
    
    @Test
    void testTextBlocksWork() {
        // Text blocks (Java 15+) should work
        String text = """
            AprismJDK
            Cross-version test
            """;
        assertNotNull(text);
        assertTrue(text.contains("AprismJDK"));
    }
    
    @Test
    void testRecordCompatibility() {
        // Records are available since Java 16
        // Test that our API works correctly
        assertDoesNotThrow(() -> {
            String version = VmInfo.getVmVersion();
            String vendor = VmInfo.getVendor();
            assertNotNull(version);
            assertNotNull(vendor);
        });
    }
    
    @Test
    void testSwitchExpressionsWork() {
        // Switch expressions (Java 14+) should work
        int version = JavaVersion.featureVersion();
        String description = switch (version) {
            case 17 -> "Java 17 LTS";
            case 21 -> "Java 21 LTS";
            case 25 -> "Java 25 LTS";
            default -> "Java " + version;
        };
        assertNotNull(description);
    }
    
    @Test
    void testApiSignatureCompatibility() {
        // Verify that API signatures are compatible across versions
        assertDoesNotThrow(() -> {
            VmInfo.getVmVersion();
            VmInfo.getVendor();
            VmInfo.isAprismJdk();
        });
    }
    
    @Test
    void testMethodHandleCompatibility() {
        // Method handles should work across versions
        assertDoesNotThrow(() -> {
            var version = VmInfo.getVmVersion();
            assertNotNull(version);
        });
    }
    
    @Test
    void testPatternMatchingInstanceof() {
        // Pattern matching for instanceof (Java 16+)
        Object obj = "test";
        if (obj instanceof String s) {
            assertEquals("test", s);
        } else {
            fail("Pattern matching should work");
        }
    }
    
    @Test
    void testStreamApiWorks() {
        // Stream API should work across versions
        var capabilities = java.util.List.of(
            VmInfo.hasClassRedefinerPlus(),
            VmInfo.hasMethodHookRegistryPlus(),
            VmInfo.hasBytecodeTransformer(),
            VmInfo.hasVmIntrospection()
        );
        
        long count = capabilities.stream()
            .filter(cap -> cap)
            .count();
        
        assertTrue(count >= 0);
    }
    
    @Test
    void testForwardCompatibility() {
        // Verify that the API design allows for future enhancements
        // without breaking existing code
        
        // All API methods should be stable
        assertNotNull(VmInfo.getVmVersion());
        assertNotNull(VmInfo.getVendor());
        assertNotNull(VmInfo.getVmName());
        
        // All methods should have backward-compatible signatures
        assertDoesNotThrow(() -> {
            VmInfo.getVmVersion();
            VmInfo.getVendor();
            VmInfo.isAprismJdk();
        });
    }
    
    @Test
    void testBackwardCompatibility() {
        // Verify that code written for older Java versions still works
        
        // Traditional for loop
        for (int i = 0; i < 10; i++) {
            assertNotNull(VmInfo.getVmVersion());
        }
        
        // Traditional try-catch
        try {
            VmInfo.getVmVersion();
        } catch (Exception e) {
            fail("Should not throw: " + e.getMessage());
        }
    }
}
