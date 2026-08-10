package jdk.aprismate.runtime;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CapabilityTest {
    
    @Test
    void testBasicCapabilityCreation() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .enable(Capability.AGENT_LOGGING)
            .enable(Capability.AGENT_INSTRUMENTATION)
            .build();
        
        assertEquals("26.1-Alpha.1", cap.getVersion());
        assertTrue(cap.isEnabled(Capability.AGENT_LOGGING));
        assertTrue(cap.isEnabled(Capability.AGENT_INSTRUMENTATION));
    }
    
    @Test
    void testDisabledCapabilities() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .enable(Capability.AGENT_LOGGING)
            .disable(Capability.TRANSFORM_REDEFINE)
            .build();
        
        assertTrue(cap.isEnabled(Capability.AGENT_LOGGING));
        assertFalse(cap.isEnabled(Capability.TRANSFORM_REDEFINE));
    }
    
    @Test
    void testUnknownCapabilityReturnsFalse() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .enable(Capability.AGENT_LOGGING)
            .build();
        
        assertFalse(cap.isEnabled("unknown.capability"));
    }
    
    @Test
    void testGetEnabledCapabilities() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .enable(Capability.AGENT_LOGGING)
            .enable(Capability.AGENT_INSTRUMENTATION)
            .disable(Capability.TRANSFORM_REDEFINE)
            .build();
        
        Set<String> enabled = cap.getEnabledCapabilities();
        
        assertTrue(enabled.contains(Capability.AGENT_LOGGING));
        assertTrue(enabled.contains(Capability.AGENT_INSTRUMENTATION));
        assertFalse(enabled.contains(Capability.TRANSFORM_REDEFINE));
    }
    
    @Test
    void testGetAllCapabilities() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .enable(Capability.AGENT_LOGGING)
            .disable(Capability.TRANSFORM_REDEFINE)
            .build();
        
        Set<String> all = cap.getAllCapabilities();
        
        assertTrue(all.contains(Capability.AGENT_LOGGING));
        assertTrue(all.contains(Capability.TRANSFORM_REDEFINE));
    }
    
    @Test
    void testSetMethod() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .set(Capability.AGENT_LOGGING, true)
            .set(Capability.TRANSFORM_REDEFINE, false)
            .build();
        
        assertTrue(cap.isEnabled(Capability.AGENT_LOGGING));
        assertFalse(cap.isEnabled(Capability.TRANSFORM_REDEFINE));
    }
    
    @Test
    void testToString() {
        Capability cap = Capability.builder("26.1-Alpha.1")
            .enable(Capability.AGENT_LOGGING)
            .build();
        
        String str = cap.toString();
        
        assertTrue(str.contains("26.1-Alpha.1"));
        assertTrue(str.contains(Capability.AGENT_LOGGING));
    }
    
    @Test
    void testEmptyCapability() {
        Capability cap = Capability.builder("26.1-Alpha.1").build();
        
        assertEquals("26.1-Alpha.1", cap.getVersion());
        assertTrue(cap.getEnabledCapabilities().isEmpty());
    }
    
    @Test
    void testCapabilityConstants() {
        // Verify all capability constants are defined
        assertNotNull(Capability.AGENT_LOGGING);
        assertNotNull(Capability.AGENT_INSTRUMENTATION);
        assertNotNull(Capability.AGENT_FAILSAFE);
        assertNotNull(Capability.TRANSFORM_RETRANSFORM);
        assertNotNull(Capability.TRANSFORM_REDEFINE);
        assertNotNull(Capability.TRANSFORM_STRUCTURAL);
        assertNotNull(Capability.HOOK_METHOD_ENTRY);
        assertNotNull(Capability.HOOK_METHOD_EXIT);
        assertNotNull(Capability.HOOK_JIT_SURVIVAL);
        assertNotNull(Capability.INTROSPECT_THREADS);
        assertNotNull(Capability.INTROSPECT_HEAP);
        assertNotNull(Capability.INTROSPECT_JIT);
        assertNotNull(Capability.RESOURCE_MANAGER);
        assertNotNull(Capability.RESOURCE_LEAK_DETECTION);
    }
}
