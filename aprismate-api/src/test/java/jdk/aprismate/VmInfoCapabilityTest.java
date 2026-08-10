package jdk.aprismate;

import jdk.aprismate.runtime.Capability;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VmInfoCapabilityTest {
    
    @Test
    void testGetCapabilities() {
        Capability cap = VmInfo.getCapabilities();
        
        assertNotNull(cap, "Capabilities should not be null");
        assertNotNull(cap.getVersion(), "Version should not be null");
    }
    
    @Test
    void testIsCapabilityEnabled() {
        // v26.1-Alpha.1 should have agent capabilities enabled
        assertTrue(VmInfo.isCapabilityEnabled(Capability.AGENT_LOGGING),
            "Agent logging should be enabled in v26.1-Alpha.1");
        assertTrue(VmInfo.isCapabilityEnabled(Capability.AGENT_INSTRUMENTATION),
            "Agent instrumentation should be enabled in v26.1-Alpha.1");
        assertTrue(VmInfo.isCapabilityEnabled(Capability.AGENT_FAILSAFE),
            "Agent failsafe should be enabled in v26.1-Alpha.1");
    }
    
    @Test
    void testFutureCapabilitiesDisabled() {
        // Future capabilities should be disabled until implemented
        assertFalse(VmInfo.isCapabilityEnabled(Capability.TRANSFORM_STRUCTURAL),
            "Structural transform should be disabled until v26.1-Alpha.2");
        assertFalse(VmInfo.isCapabilityEnabled(Capability.HOOK_METHOD_ENTRY),
            "Method hooks should be disabled until v26.1-Alpha.4");
        assertFalse(VmInfo.isCapabilityEnabled(Capability.INTROSPECT_THREADS),
            "Thread introspection should be disabled until v26.1-Alpha.6");
    }
    
    @Test
    void testV26CapabilitiesEnabled() {
        // v26.0 capabilities should still be enabled
        assertTrue(VmInfo.isCapabilityEnabled(Capability.RESOURCE_MANAGER),
            "Resource manager should be enabled from v26.0");
        assertTrue(VmInfo.isCapabilityEnabled(Capability.RESOURCE_LEAK_DETECTION),
            "Resource leak detection should be enabled from v26.0");
    }
    
    @Test
    void testCapabilityQueryIntegration() {
        Capability cap = VmInfo.getCapabilities();
        
        // Query through VmInfo should match query through Capability
        assertEquals(cap.isEnabled(Capability.AGENT_LOGGING), 
            VmInfo.isCapabilityEnabled(Capability.AGENT_LOGGING),
            "VmInfo and Capability queries should return same result");
    }
    
    @Test
    void testGetEnabledCapabilities() {
        Capability cap = VmInfo.getCapabilities();
        var enabled = cap.getEnabledCapabilities();
        
        assertTrue(enabled.contains(Capability.AGENT_LOGGING));
        assertTrue(enabled.contains(Capability.AGENT_INSTRUMENTATION));
        assertTrue(enabled.contains(Capability.AGENT_FAILSAFE));
        assertTrue(enabled.contains(Capability.RESOURCE_MANAGER));
        assertTrue(enabled.contains(Capability.RESOURCE_LEAK_DETECTION));
    }
}
