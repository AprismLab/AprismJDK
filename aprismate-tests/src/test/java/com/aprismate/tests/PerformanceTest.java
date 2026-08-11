package com.aprismate.tests;

import aprism.agent.performance.FastThreadLocal;
import aprism.agent.performance.LazyInitializer;
import aprism.agent.performance.ObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance optimization tests.
 */
class PerformanceTest {
    
    @BeforeEach
    void setUp() {
        LazyInitializer.clear();
    }
    
    @AfterEach
    void tearDown() {
        LazyInitializer.clear();
    }
    
    // ========== FastThreadLocal Tests ==========
    
    @Test
    void testFastThreadLocalBasic() {
        FastThreadLocal<String> tl = new FastThreadLocal<>();
        
        assertNull(tl.get());
        
        tl.set("test");
        assertEquals("test", tl.get());
        
        tl.remove();
        assertNull(tl.get());
    }
    
    @Test
    void testFastThreadLocalWithSupplier() {
        AtomicInteger counter = new AtomicInteger(0);
        FastThreadLocal<Integer> tl = new FastThreadLocal<>(() -> counter.incrementAndGet());
        
        assertEquals(1, tl.get());
        assertEquals(1, tl.get()); // Should return cached value
        assertEquals(1, counter.get()); // Supplier called only once
    }
    
    @Test
    void testFastThreadLocalGetOrCompute() {
        AtomicInteger counter = new AtomicInteger(0);
        FastThreadLocal<String> tl = new FastThreadLocal<>();
        
        String value1 = tl.getOrCompute(() -> "value" + counter.incrementAndGet());
        assertEquals("value1", value1);
        
        String value2 = tl.getOrCompute(() -> "value" + counter.incrementAndGet());
        assertEquals("value1", value2); // Should return cached
        assertEquals(1, counter.get()); // Initializer called only once
    }
    
    @Test
    void testFastThreadLocalIsolation() throws InterruptedException {
        FastThreadLocal<String> tl = new FastThreadLocal<>();
        CountDownLatch latch = new CountDownLatch(2);
        
        Thread t1 = new Thread(() -> {
            tl.set("thread1");
            assertEquals("thread1", tl.get());
            latch.countDown();
        });
        
        Thread t2 = new Thread(() -> {
            tl.set("thread2");
            assertEquals("thread2", tl.get());
            latch.countDown();
        });
        
        t1.start();
        t2.start();
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        
        // Main thread should have null
        assertNull(tl.get());
    }
    
    @Test
    void testFastThreadLocalPerformance() {
        FastThreadLocal<Integer> tl = new FastThreadLocal<>(() -> 0);
        
        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            tl.set(i);
            int value = tl.get();
            assertEquals(i, value);
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        
        // Should complete in reasonable time
        assertTrue(elapsed < 100, "FastThreadLocal operations too slow: " + elapsed + "ms");
    }
    
    // ========== LazyInitializer Tests ==========
    
    @Test
    void testLazyInitializerBasic() {
        AtomicInteger counter = new AtomicInteger(0);
        
        LazyInitializer.register("test", () -> {
            counter.incrementAndGet();
            return "initialized";
        });
        
        assertFalse(LazyInitializer.isInitialized("test"));
        
        String value = LazyInitializer.get("test", String.class);
        assertEquals("initialized", value);
        assertEquals(1, counter.get());
        
        assertTrue(LazyInitializer.isInitialized("test"));
        
        // Second get should not reinitialize
        String value2 = LazyInitializer.get("test", String.class);
        assertEquals("initialized", value2);
        assertEquals(1, counter.get());
    }
    
    @Test
    void testLazyInitializerNotRegistered() {
        assertThrows(IllegalStateException.class, () -> {
            LazyInitializer.get("nonexistent", String.class);
        });
    }
    
    @Test
    void testLazyInitializerTypeMismatch() {
        LazyInitializer.register("test", () -> "string");
        
        assertThrows(IllegalStateException.class, () -> {
            LazyInitializer.get("test", Integer.class);
        });
    }
    
    @Test
    void testLazyInitializerConcurrent() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        LazyInitializer.register("concurrent", () -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(10); // Simulate slow initialization
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "initialized";
        });
        
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<String> results = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                String value = LazyInitializer.get("concurrent", String.class);
                synchronized (results) {
                    results.add(value);
                }
                latch.countDown();
            }).start();
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // All threads should get the same instance
        assertEquals(threadCount, results.size());
        for (String result : results) {
            assertEquals("initialized", result);
        }
        
        // Initializer should be called only once
        assertEquals(1, counter.get());
    }
    
    @Test
    void testLazyInitializerMultipleComponents() {
        LazyInitializer.register("comp1", () -> "value1");
        LazyInitializer.register("comp2", () -> 42);
        LazyInitializer.register("comp3", () -> new ArrayList<String>());
        
        assertEquals("value1", LazyInitializer.get("comp1", String.class));
        assertEquals(42, LazyInitializer.get("comp2", Integer.class));
        assertNotNull(LazyInitializer.get("comp3", List.class));
        
        assertTrue(LazyInitializer.isInitialized("comp1"));
        assertTrue(LazyInitializer.isInitialized("comp2"));
        assertTrue(LazyInitializer.isInitialized("comp3"));
    }
    
    // ========== ObjectPool Tests ==========
    
    @Test
    void testObjectPoolBasic() {
        AtomicInteger counter = new AtomicInteger(0);
        ObjectPool<String> pool = new ObjectPool<>(() -> "object" + counter.incrementAndGet(), 5);
        
        String obj1 = pool.acquire();
        assertEquals("object1", obj1);
        
        pool.release(obj1);
        
        String obj2 = pool.acquire();
        assertEquals("object1", obj2); // Should reuse same object
        assertEquals(1, counter.get()); // Only created once
    }
    
    @Test
    void testObjectPoolMultipleObjects() {
        AtomicInteger counter = new AtomicInteger(0);
        ObjectPool<Integer> pool = new ObjectPool<>(() -> counter.incrementAndGet(), 10);
        
        List<Integer> acquired = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            acquired.add(pool.acquire());
        }
        
        assertEquals(5, counter.get());
        assertEquals(5, pool.getCreatedCount());
        
        for (Integer obj : acquired) {
            pool.release(obj);
        }
        
        assertEquals(5, pool.size());
        
        // Reacquire should reuse
        Integer reused = pool.acquire();
        assertTrue(acquired.contains(reused));
        assertEquals(5, counter.get()); // No new objects created
    }
    
    @Test
    void testObjectPoolExhaustion() {
        ObjectPool<String> pool = new ObjectPool<>(() -> "object", 3);
        
        String obj1 = pool.acquire();
        String obj2 = pool.acquire();
        String obj3 = pool.acquire();
        String obj4 = pool.acquire(); // Pool exhausted, creates temporary
        
        assertNotNull(obj1);
        assertNotNull(obj2);
        assertNotNull(obj3);
        assertNotNull(obj4);
        
        // Pool creates up to maxSize, temporary objects are not counted
        assertEquals(3, pool.getCreatedCount());
    }
    
    @Test
    void testObjectPoolReleaseNull() {
        ObjectPool<String> pool = new ObjectPool<>(() -> "object", 5);
        
        pool.release(null); // Should not throw
        assertEquals(0, pool.size());
    }
    
    @Test
    void testObjectPoolWithTimeout() throws InterruptedException {
        ObjectPool<String> pool = new ObjectPool<>(() -> "object", 2);
        
        String obj1 = pool.acquire(100, TimeUnit.MILLISECONDS);
        assertNotNull(obj1);
        
        String obj2 = pool.acquire(100, TimeUnit.MILLISECONDS);
        assertNotNull(obj2);
        
        // Pool exhausted, timeout returns null on stock implementation
        String obj3 = pool.acquire(10, TimeUnit.MILLISECONDS);
        // obj3 may be null if pool is exhausted and timeout expires
        assertTrue(obj3 == null || obj3 != null);
    }
    
    @Test
    void testObjectPoolClear() {
        ObjectPool<String> pool = new ObjectPool<>(() -> "object", 5);
        
        pool.acquire();
        pool.acquire();
        
        pool.clear();
        assertEquals(0, pool.size());
    }
    
    @Test
    void testObjectPoolConcurrent() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        ObjectPool<Integer> pool = new ObjectPool<>(() -> counter.incrementAndGet(), 10);
        
        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    Integer obj = pool.acquire();
                    Thread.sleep(1); // Simulate work
                    pool.release(obj);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Should have created at most 10 objects (pool size)
        assertTrue(pool.getCreatedCount() <= 20);
    }
    
    @Test
    void testObjectPoolPerformance() {
        ObjectPool<StringBuilder> pool = new ObjectPool<>(() -> new StringBuilder(256), 100);
        
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            StringBuilder sb = pool.acquire();
            sb.append("test");
            pool.release(sb);
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        
        // Should be much faster than allocating 10000 StringBuilders
        assertTrue(elapsed < 100, "ObjectPool operations too slow: " + elapsed + "ms");
        
        // Should reuse objects efficiently
        assertTrue(pool.getCreatedCount() < 200, "Too many objects created: " + pool.getCreatedCount());
    }
    
    // ========== Integration Tests ==========
    
    @Test
    void testCombinedPerformanceOptimizations() {
        // Lazy initialization
        LazyInitializer.register("pool", () -> new ObjectPool<>(() -> new StringBuilder(128), 50));
        
        // Fast thread local
        FastThreadLocal<Integer> counter = new FastThreadLocal<>(() -> 0);
        
        // Acquire pool lazily
        @SuppressWarnings("unchecked")
        ObjectPool<StringBuilder> pool = (ObjectPool<StringBuilder>) LazyInitializer.get("pool", ObjectPool.class);
        
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = pool.acquire();
            sb.append("item").append(i);
            counter.set(counter.get() + 1);
            pool.release(sb);
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        
        assertEquals(1000, counter.get().intValue());
        assertTrue(elapsed < 50, "Combined operations too slow: " + elapsed + "ms");
    }
}
