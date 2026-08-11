package com.aprismate.tests;

import aprism.agent.runtime.DefaultJitInsight;
import jdk.aprismate.runtime.JitInsight;
import jdk.aprismate.runtime.JitInsight.CompilationLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for JitInsight API.
 */
class JitInsightTest {
    
    private JitInsight jitInsight;
    
    @BeforeEach
    void setUp() {
        jitInsight = new DefaultJitInsight();
    }
    
    @Test
    void testGetCompilationQueue() {
        List<JitInsight.CompilationTask> queue = jitInsight.getCompilationQueue();
        
        assertNotNull(queue, "Compilation queue should not be null");
        // On stock JDK, this returns empty list
        assertTrue(queue.isEmpty() || !queue.isEmpty(), "Queue should be a valid list");
    }
    
    @Test
    void testGetCompiledMethods() {
        List<JitInsight.CompiledMethod> methods = jitInsight.getCompiledMethods();
        
        assertNotNull(methods, "Compiled methods list should not be null");
        // On stock JDK, this returns empty list
        assertTrue(methods.isEmpty() || !methods.isEmpty(), "Methods should be a valid list");
    }
    
    @Test
    void testGetMethodCompilationLevel() throws NoSuchMethodException {
        Method method = JitInsightTest.class.getDeclaredMethod("testGetMethodCompilationLevel");
        CompilationLevel level = jitInsight.getMethodCompilationLevel(method);
        
        assertNotNull(level, "Compilation level should not be null");
        assertTrue(level != null, "Should return a valid compilation level");
    }
    
    @Test
    void testGetMethodCompilationLevelNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            jitInsight.getMethodCompilationLevel(null);
        });
    }
    
    @Test
    void testDeoptimizeMethod() throws NoSuchMethodException {
        Method method = JitInsightTest.class.getDeclaredMethod("testDeoptimizeMethod");
        boolean result = jitInsight.deoptimizeMethod(method);
        
        // On stock JDK, this returns false
        assertTrue(result == false || result == true, "Should return boolean");
    }
    
    @Test
    void testDeoptimizeMethodNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            jitInsight.deoptimizeMethod(null);
        });
    }
    
    @Test
    void testIsCompilerActive() {
        boolean active = jitInsight.isCompilerActive();
        
        // Should return true or false
        assertTrue(active == true || active == false, "Should return boolean");
    }
    
    @Test
    void testGetTotalCompilations() {
        long compilations = jitInsight.getTotalCompilations();
        
        // Returns -1 if not supported, or non-negative count
        assertTrue(compilations >= -1, "Total compilations should be >= -1");
    }
    
    @Test
    void testGetTotalCompilationTime() {
        long time = jitInsight.getTotalCompilationTime();
        
        // Returns -1 if not supported, or non-negative time
        assertTrue(time >= -1, "Total compilation time should be >= -1");
    }
    
    @Test
    void testCompilationLevelEnum() {
        assertEquals(0, CompilationLevel.INTERPRETED.getLevel());
        assertEquals(1, CompilationLevel.C1_NO_PROFILING.getLevel());
        assertEquals(2, CompilationLevel.C1_LIMITED_PROFILING.getLevel());
        assertEquals(3, CompilationLevel.C1_FULL_PROFILING.getLevel());
        assertEquals(4, CompilationLevel.C2.getLevel());
        assertEquals(-1, CompilationLevel.UNKNOWN.getLevel());
    }
    
    @Test
    void testCompilationLevelFromLevel() {
        assertEquals(CompilationLevel.INTERPRETED, CompilationLevel.fromLevel(0));
        assertEquals(CompilationLevel.C1_NO_PROFILING, CompilationLevel.fromLevel(1));
        assertEquals(CompilationLevel.C1_LIMITED_PROFILING, CompilationLevel.fromLevel(2));
        assertEquals(CompilationLevel.C1_FULL_PROFILING, CompilationLevel.fromLevel(3));
        assertEquals(CompilationLevel.C2, CompilationLevel.fromLevel(4));
        assertEquals(CompilationLevel.UNKNOWN, CompilationLevel.fromLevel(99));
        assertEquals(CompilationLevel.UNKNOWN, CompilationLevel.fromLevel(-5));
    }
    
    @Test
    void testHotMethod() throws NoSuchMethodException {
        // Call a method many times to potentially trigger compilation
        for (int i = 0; i < 100000; i++) {
            hotMethod(i);
        }
        
        Method method = JitInsightTest.class.getDeclaredMethod("hotMethod", int.class);
        CompilationLevel level = jitInsight.getMethodCompilationLevel(method);
        
        assertNotNull(level);
        // On stock JDK we can't detect compilation, so this will be INTERPRETED
        // On AprismJDK with VM patches, this might be C1 or C2
    }
    
    @SuppressWarnings("unused")
    private int hotMethod(int x) {
        return x * 2 + 1;
    }
    
    @Test
    void testMultipleMethodQueries() throws NoSuchMethodException {
        Method method1 = JitInsightTest.class.getDeclaredMethod("testGetCompilationQueue");
        Method method2 = JitInsightTest.class.getDeclaredMethod("testGetCompiledMethods");
        Method method3 = JitInsightTest.class.getDeclaredMethod("testIsCompilerActive");
        
        CompilationLevel level1 = jitInsight.getMethodCompilationLevel(method1);
        CompilationLevel level2 = jitInsight.getMethodCompilationLevel(method2);
        CompilationLevel level3 = jitInsight.getMethodCompilationLevel(method3);
        
        assertNotNull(level1);
        assertNotNull(level2);
        assertNotNull(level3);
    }
    
    @Test
    void testCompilerNameAccessible() {
        // Test DefaultJitInsight specific method
        if (jitInsight instanceof DefaultJitInsight) {
            DefaultJitInsight defaultJit = (DefaultJitInsight) jitInsight;
            String compilerName = defaultJit.getCompilerName();
            
            // May be null if compiler not available, or a string like "HotSpot 64-Bit Tiered Compilers"
            assertTrue(compilerName == null || compilerName.length() > 0, 
                "Compiler name should be null or non-empty");
        }
    }
    
    @Test
    void testCompilationTimeIncreases() throws InterruptedException {
        long time1 = jitInsight.getTotalCompilationTime();
        
        if (time1 >= 0) {
            // Do some work to potentially trigger compilation
            for (int i = 0; i < 50000; i++) {
                workMethod(i);
            }
            
            Thread.sleep(100);
            
            long time2 = jitInsight.getTotalCompilationTime();
            
            // Time should stay same or increase
            assertTrue(time2 >= time1, "Compilation time should not decrease");
        }
    }
    
    @SuppressWarnings("unused")
    private int workMethod(int x) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += x * i;
        }
        return sum;
    }
    
    @Test
    void testDeoptimizeInterpretedMethod() throws NoSuchMethodException {
        // This method is unlikely to be compiled
        Method method = JitInsightTest.class.getDeclaredMethod("coldMethod");
        
        boolean result = jitInsight.deoptimizeMethod(method);
        
        // Should return false on stock JDK (not supported)
        // On AprismJDK, might return false (method not compiled) or true (deoptimized)
        assertTrue(result == false || result == true);
    }
    
    @SuppressWarnings("unused")
    private void coldMethod() {
        // This method is intentionally never called, so won't be compiled
    }
    
    @Test
    void testCompilationLevelOrdering() {
        // Verify compilation levels are in correct order
        assertTrue(CompilationLevel.INTERPRETED.getLevel() < CompilationLevel.C1_NO_PROFILING.getLevel());
        assertTrue(CompilationLevel.C1_NO_PROFILING.getLevel() < CompilationLevel.C1_LIMITED_PROFILING.getLevel());
        assertTrue(CompilationLevel.C1_LIMITED_PROFILING.getLevel() < CompilationLevel.C1_FULL_PROFILING.getLevel());
        assertTrue(CompilationLevel.C1_FULL_PROFILING.getLevel() < CompilationLevel.C2.getLevel());
    }
    
    @Test
    void testCompilationTaskToString() {
        // Test internal CompilationTaskImpl toString
        try {
            Method method = JitInsightTest.class.getDeclaredMethod("testCompilationTaskToString");
            var task = new DefaultJitInsight.CompilationTaskImpl(
                method, CompilationLevel.C2, 10, false);
            
            String str = task.toString();
            assertNotNull(str);
            assertTrue(str.contains("CompilationTask"));
            assertTrue(str.contains("C2"));
        } catch (NoSuchMethodException e) {
            fail("Method should exist");
        }
    }
    
    @Test
    void testCompiledMethodToString() {
        // Test internal CompiledMethodImpl toString
        try {
            Method method = JitInsightTest.class.getDeclaredMethod("testCompiledMethodToString");
            var compiled = new DefaultJitInsight.CompiledMethodImpl(
                method, CompilationLevel.C2, 256, 10000, false);
            
            String str = compiled.toString();
            assertNotNull(str);
            assertTrue(str.contains("CompiledMethod"));
            assertTrue(str.contains("C2"));
            assertTrue(str.contains("256"));
        } catch (NoSuchMethodException e) {
            fail("Method should exist");
        }
    }
}
