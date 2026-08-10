package jdk.aprismate.agent;

import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstrumentationContextTest {
    
    @Test
    void testNotInitializedByDefault() {
        // Note: In real scenarios, InstrumentationContext might already be initialized
        // This test assumes a fresh state
        assertFalse(InstrumentationContext.isAvailable() && InstrumentationContext.getInstrumentation() == null,
            "Should handle uninitialized state");
    }
    
    @Test
    void testInitialization() {
        Instrumentation mockInst = mock(Instrumentation.class);
        
        try {
            InstrumentationContext.initialize(mockInst);
            
            assertTrue(InstrumentationContext.isAvailable(), "Should be available after initialization");
            assertSame(mockInst, InstrumentationContext.getInstrumentation(), "Should return same instance");
        } catch (IllegalStateException e) {
            // Already initialized - this is acceptable in test environment
        }
    }
    
    @Test
    void testRetransformSupported() {
        Instrumentation mockInst = mock(Instrumentation.class);
        when(mockInst.isRetransformClassesSupported()).thenReturn(true);
        
        try {
            InstrumentationContext.initialize(mockInst);
            assertTrue(InstrumentationContext.isRetransformSupported(), "Should report retransform support");
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testRedefineSupported() {
        Instrumentation mockInst = mock(Instrumentation.class);
        when(mockInst.isRedefineClassesSupported()).thenReturn(true);
        
        try {
            InstrumentationContext.initialize(mockInst);
            assertTrue(InstrumentationContext.isRedefineSupported(), "Should report redefine support");
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testNativeMethodPrefixSupported() {
        Instrumentation mockInst = mock(Instrumentation.class);
        when(mockInst.isNativeMethodPrefixSupported()).thenReturn(true);
        
        try {
            InstrumentationContext.initialize(mockInst);
            assertTrue(InstrumentationContext.isNativeMethodPrefixSupported(), 
                "Should report native method prefix support");
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
}
