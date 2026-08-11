package com.aprismate.tests;

import aprism.agent.runtime.DefaultHeapInsight;
import jdk.aprismate.runtime.HeapInsight;
import jdk.aprismate.runtime.HeapInsight.HeapRegion;
import jdk.aprismate.runtime.HeapInsight.RegionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for HeapInsight API.
 */
class HeapInsightTest {
    
    private HeapInsight heapInsight;
    private Instrumentation mockInstrumentation;
    
    @BeforeEach
    void setUp() {
        mockInstrumentation = Mockito.mock(Instrumentation.class);
        heapInsight = new DefaultHeapInsight(mockInstrumentation);
    }
    
    @Test
    void testGetHeapRegions() {
        List<HeapRegion> regions = heapInsight.getHeapRegions();
        
        assertNotNull(regions, "Heap regions should not be null");
        assertFalse(regions.isEmpty(), "Should have at least one heap region");
    }
    
    @Test
    void testHeapRegionsHaveValidTypes() {
        List<HeapRegion> regions = heapInsight.getHeapRegions();
        
        for (HeapRegion region : regions) {
            assertNotNull(region.getType(), "Region type should not be null");
            assertTrue(region.getSize() >= 0, "Region size should be non-negative");
            assertTrue(region.getUsed() >= 0, "Used bytes should be non-negative");
            assertTrue(region.getFree() >= 0, "Free bytes should be non-negative");
        }
    }
    
    @Test
    void testHeapRegionSizeConsistency() {
        List<HeapRegion> regions = heapInsight.getHeapRegions();
        
        for (HeapRegion region : regions) {
            long size = region.getSize();
            long used = region.getUsed();
            long free = region.getFree();
            
            // Used + Free should approximately equal Size
            // (may not be exact due to rounding or concurrent changes)
            assertTrue(used + free <= size * 1.1, 
                "Used + Free should be approximately equal to Size");
        }
    }
    
    @Test
    void testGetObjectSize() {
        Object obj = new Object();
        
        // Mock the instrumentation to return a size
        Mockito.when(mockInstrumentation.getObjectSize(obj)).thenReturn(16L);
        
        long size = heapInsight.getObjectSize(obj);
        
        assertTrue(size > 0, "Object size should be positive");
        assertEquals(16L, size, "Should match mocked size");
    }
    
    @Test
    void testGetObjectSizeForString() {
        String str = "Hello, World!";
        
        Mockito.when(mockInstrumentation.getObjectSize(str)).thenReturn(56L);
        
        long size = heapInsight.getObjectSize(str);
        
        assertTrue(size > 0, "String size should be positive");
    }
    
    @Test
    void testGetObjectSizeForArray() {
        int[] array = new int[100];
        
        Mockito.when(mockInstrumentation.getObjectSize(array)).thenReturn(416L);
        
        long size = heapInsight.getObjectSize(array);
        
        assertTrue(size > 0, "Array size should be positive");
    }
    
    @Test
    void testGetObjectSizeNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            heapInsight.getObjectSize(null);
        });
    }
    
    @Test
    void testGetRetainedSize() {
        Object obj = new Object();
        
        Mockito.when(mockInstrumentation.getObjectSize(Mockito.any())).thenReturn(16L);
        
        long size = heapInsight.getRetainedSize(obj);
        
        assertTrue(size >= 0, "Retained size should be non-negative");
    }
    
    @Test
    void testGetRetainedSizeForObjectGraph() {
        // Create a small object graph
        Object[] array = new Object[5];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Object();
        }
        
        Mockito.when(mockInstrumentation.getObjectSize(Mockito.any())).thenReturn(16L);
        
        long size = heapInsight.getRetainedSize(array);
        
        // Should include array + referenced objects
        assertTrue(size >= 16 * 6, "Retained size should include array and elements");
    }
    
    @Test
    void testGetRetainedSizeNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            heapInsight.getRetainedSize(null);
        });
    }
    
    @Test
    void testHeapRegionTypes() {
        List<HeapRegion> regions = heapInsight.getHeapRegions();
        
        // Check that we have some standard region types
        boolean hasYoungOrOld = false;
        
        for (HeapRegion region : regions) {
            RegionType type = region.getType();
            if (type == RegionType.YOUNG || type == RegionType.OLD || 
                type == RegionType.METASPACE) {
                hasYoungOrOld = true;
                break;
            }
        }
        
        assertTrue(hasYoungOrOld, "Should have at least one standard heap region");
    }
    
    @Test
    void testMultipleHeapRegionQueries() {
        // Query multiple times to ensure consistency
        List<HeapRegion> regions1 = heapInsight.getHeapRegions();
        List<HeapRegion> regions2 = heapInsight.getHeapRegions();
        
        assertNotNull(regions1);
        assertNotNull(regions2);
        
        // Should have same number of regions (assuming no GC changes)
        assertEquals(regions1.size(), regions2.size(), 
            "Region count should be consistent");
    }
    
    @Test
    void testGetObjectSizeWithoutInstrumentation() {
        HeapInsight noInstHeapInsight = new DefaultHeapInsight(null);
        
        Object obj = new Object();
        long size = noInstHeapInsight.getObjectSize(obj);
        
        assertEquals(-1, size, "Should return -1 when instrumentation unavailable");
    }
    
    @Test
    void testGetRetainedSizeWithoutInstrumentation() {
        HeapInsight noInstHeapInsight = new DefaultHeapInsight(null);
        
        Object obj = new Object();
        long size = noInstHeapInsight.getRetainedSize(obj);
        
        assertEquals(-1, size, "Should return -1 when instrumentation unavailable");
    }
    
    @Test
    void testLargeObjectSize() {
        byte[] largeArray = new byte[1024 * 1024]; // 1MB
        
        Mockito.when(mockInstrumentation.getObjectSize(largeArray))
            .thenReturn(1024L * 1024L + 16L);
        
        long size = heapInsight.getObjectSize(largeArray);
        
        assertTrue(size > 1024 * 1024, "Large array should have size > 1MB");
    }
    
    @Test
    void testHeapRegionToString() {
        List<HeapRegion> regions = heapInsight.getHeapRegions();
        
        if (!regions.isEmpty()) {
            HeapRegion region = regions.get(0);
            String str = region.toString();
            
            assertNotNull(str);
            assertFalse(str.isEmpty());
            assertTrue(str.contains("HeapRegion"), "toString should contain class name");
        }
    }
    
    @Test
    void testConcurrentObjectAllocation() throws InterruptedException {
        List<Object> objects = new ArrayList<>();
        
        // Allocate objects in multiple threads
        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    synchronized (objects) {
                        objects.add(new Object());
                    }
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Query heap regions after allocation
        List<HeapRegion> regions = heapInsight.getHeapRegions();
        
        assertNotNull(regions);
        assertFalse(regions.isEmpty());
    }
}
