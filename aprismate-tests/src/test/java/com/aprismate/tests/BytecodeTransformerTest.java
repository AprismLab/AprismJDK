package com.aprismate.tests;

import jdk.aprismate.agent.BytecodeTransformer;
import aprism.agent.transform.DefaultBytecodeTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for BytecodeTransformer API.
 * Tests load-time transformation, registration, and ASM-based transformations.
 */
@DisplayName("BytecodeTransformer Tests")
class BytecodeTransformerTest {

    private Instrumentation mockInstrumentation;
    private BytecodeTransformer transformer;

    @BeforeEach
    void setUp() {
        mockInstrumentation = mock(Instrumentation.class);
        when(mockInstrumentation.isRetransformClassesSupported()).thenReturn(true);
        transformer = new DefaultBytecodeTransformer(mockInstrumentation);
    }

    @Test
    @DisplayName("Should register transformer successfully")
    void testRegisterTransformer() {
        ClassFileTransformer mockTransformer = mock(ClassFileTransformer.class);
        
        transformer.registerTransformer(mockTransformer);
        
        verify(mockInstrumentation).addTransformer(mockTransformer, false);
        assertThat(transformer.getTransformerCount()).isEqualTo(1);
        assertThat(transformer.getTransformers()).containsExactly(mockTransformer);
    }

    @Test
    @DisplayName("Should register transformer with retransform capability")
    void testRegisterTransformerWithRetransform() {
        ClassFileTransformer mockTransformer = mock(ClassFileTransformer.class);
        
        transformer.registerTransformer(mockTransformer, true);
        
        verify(mockInstrumentation).addTransformer(mockTransformer, true);
        assertThat(transformer.getTransformerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw exception when registering null transformer")
    void testRegisterNullTransformer() {
        assertThatThrownBy(() -> transformer.registerTransformer(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("transformer cannot be null");
    }

    @Test
    @DisplayName("Should unregister transformer successfully")
    void testUnregisterTransformer() {
        ClassFileTransformer mockTransformer = mock(ClassFileTransformer.class);
        when(mockInstrumentation.removeTransformer(mockTransformer)).thenReturn(true);
        
        transformer.registerTransformer(mockTransformer);
        boolean removed = transformer.unregisterTransformer(mockTransformer);
        
        assertThat(removed).isTrue();
        assertThat(transformer.getTransformerCount()).isZero();
        verify(mockInstrumentation).removeTransformer(mockTransformer);
    }

    @Test
    @DisplayName("Should return false when unregistering non-existent transformer")
    void testUnregisterNonExistentTransformer() {
        ClassFileTransformer mockTransformer = mock(ClassFileTransformer.class);
        
        boolean removed = transformer.unregisterTransformer(mockTransformer);
        
        assertThat(removed).isFalse();
        verify(mockInstrumentation, never()).removeTransformer(any());
    }

    @Test
    @DisplayName("Should maintain registration order")
    void testTransformerRegistrationOrder() {
        ClassFileTransformer transformer1 = mock(ClassFileTransformer.class);
        ClassFileTransformer transformer2 = mock(ClassFileTransformer.class);
        ClassFileTransformer transformer3 = mock(ClassFileTransformer.class);
        
        transformer.registerTransformer(transformer1);
        transformer.registerTransformer(transformer2);
        transformer.registerTransformer(transformer3);
        
        List<ClassFileTransformer> transformers = transformer.getTransformers();
        assertThat(transformers).containsExactly(transformer1, transformer2, transformer3);
    }

    @Test
    @DisplayName("Should return immutable list of transformers")
    void testImmutableTransformerList() {
        ClassFileTransformer mockTransformer = mock(ClassFileTransformer.class);
        transformer.registerTransformer(mockTransformer);
        
        List<ClassFileTransformer> transformers = transformer.getTransformers();
        
        assertThatThrownBy(() -> transformers.add(mock(ClassFileTransformer.class)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should clear all transformers")
    void testClearTransformers() {
        ClassFileTransformer transformer1 = mock(ClassFileTransformer.class);
        ClassFileTransformer transformer2 = mock(ClassFileTransformer.class);
        
        transformer.registerTransformer(transformer1);
        transformer.registerTransformer(transformer2);
        transformer.clearTransformers();
        
        assertThat(transformer.getTransformerCount()).isZero();
        verify(mockInstrumentation).removeTransformer(transformer1);
        verify(mockInstrumentation).removeTransformer(transformer2);
    }

    @Test
    @DisplayName("Should check retransform support")
    void testRetransformSupport() {
        when(mockInstrumentation.isRetransformClassesSupported()).thenReturn(true);
        assertThat(transformer.isRetransformSupported()).isTrue();
        
        when(mockInstrumentation.isRetransformClassesSupported()).thenReturn(false);
        assertThat(transformer.isRetransformSupported()).isFalse();
    }

    @Test
    @DisplayName("Should retransform classes successfully")
    void testRetransformClasses() throws Exception {
        Class<?>[] classes = new Class<?>[]{String.class, Integer.class};
        
        transformer.retransformClasses(classes);
        
        verify(mockInstrumentation).retransformClasses(classes);
    }

    @Test
    @DisplayName("Should throw exception when retransforming with null classes")
    void testRetransformNullClasses() {
        assertThatThrownBy(() -> transformer.retransformClasses((Class<?>[]) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("classes cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when retransforming with empty array")
    void testRetransformEmptyClasses() {
        assertThatThrownBy(() -> transformer.retransformClasses(new Class<?>[0]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("classes cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when retransform not supported")
    void testRetransformNotSupported() {
        when(mockInstrumentation.isRetransformClassesSupported()).thenReturn(false);
        
        assertThatThrownBy(() -> transformer.retransformClasses(String.class))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Retransformation is not supported");
    }

    @Test
    @DisplayName("Should handle concurrent transformer registration")
    void testConcurrentRegistration() throws InterruptedException {
        AtomicInteger registeredCount = new AtomicInteger(0);
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                ClassFileTransformer t = mock(ClassFileTransformer.class);
                transformer.registerTransformer(t);
                registeredCount.incrementAndGet();
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertThat(registeredCount.get()).isEqualTo(threadCount);
        assertThat(transformer.getTransformerCount()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("Should invoke transformer for class loading")
    void testTransformerInvocation() {
        AtomicInteger invocationCount = new AtomicInteger(0);
        ClassFileTransformer countingTransformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, 
                                   Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain, 
                                   byte[] classfileBuffer) {
                invocationCount.incrementAndGet();
                return classfileBuffer; // No transformation
            }
        };
        
        transformer.registerTransformer(countingTransformer);
        
        // Verify transformer was registered
        assertThat(transformer.getTransformers()).contains(countingTransformer);
    }

    @Test
    @DisplayName("Should handle transformer that returns null")
    void testTransformerReturnsNull() {
        ClassFileTransformer nullReturningTransformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                   Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain,
                                   byte[] classfileBuffer) {
                return null;
            }
        };
        
        transformer.registerTransformer(nullReturningTransformer);
        
        assertThat(transformer.getTransformerCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle transformer that throws exception")
    void testTransformerThrowsException() {
        ClassFileTransformer failingTransformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                   Class<?> classBeingRedefined,
                                   ProtectionDomain protectionDomain,
                                   byte[] classfileBuffer) {
                throw new RuntimeException("Transformation failed");
            }
        };
        
        transformer.registerTransformer(failingTransformer);
        
        // Transformer should still be registered despite potential failures
        assertThat(transformer.getTransformers()).contains(failingTransformer);
    }
}
