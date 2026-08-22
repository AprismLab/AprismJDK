package com.aprismate.tests;

import aprism.agent.runtime.DefaultThreadInsight;
import jdk.aprismate.runtime.ThreadInsight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ThreadInsight API.
 */
class ThreadInsightTest {
    
    private ThreadInsight threadInsight;
    
    @BeforeEach
    void setUp() {
        threadInsight = new DefaultThreadInsight();
    }
    
    @Test
    void testGetAllThreads() {
        List<Thread> threads = threadInsight.getAllThreads();
        
        assertNotNull(threads, "Thread list should not be null");
        assertFalse(threads.isEmpty(), "Should have at least one thread");
        
        // Current thread should be in the list
        Thread currentThread = Thread.currentThread();
        assertTrue(threads.contains(currentThread), "Should contain current thread");
    }
    
    @Test
    void testGetAllThreadsIncludesNewThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean found = new AtomicBoolean(false);
        
        Thread testThread = new Thread(() -> {
            latch.countDown();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "TestThread-Insight");
        
        testThread.start();
        latch.await(1, TimeUnit.SECONDS);
        
        List<Thread> threads = threadInsight.getAllThreads();
        for (Thread t : threads) {
            if ("TestThread-Insight".equals(t.getName())) {
                found.set(true);
                break;
            }
        }
        
        testThread.join(1000);
        assertTrue(found.get(), "Should find newly created thread");
    }
    
    @Test
    void testGetThreadStack() {
        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stack = threadInsight.getThreadStack(currentThread);
        
        assertNotNull(stack, "Stack trace should not be null");
        assertTrue(stack.length > 0, "Stack trace should not be empty");
        
        // Check that current method is in the stack
        boolean foundCurrentMethod = false;
        for (StackTraceElement element : stack) {
            if (element.getMethodName().equals("testGetThreadStack")) {
                foundCurrentMethod = true;
                break;
            }
        }
        assertTrue(foundCurrentMethod, "Should find current method in stack");
    }
    
    @Test
    void testGetThreadStackForDeadThread() throws InterruptedException {
        Thread thread = new Thread(() -> {
            // Do nothing, exit immediately
        });
        
        thread.start();
        thread.join();
        
        // Thread is now dead
        StackTraceElement[] stack = threadInsight.getThreadStack(thread);
        
        assertNotNull(stack, "Should return non-null for dead thread");
        assertEquals(0, stack.length, "Should return empty array for dead thread");
    }
    
    @Test
    void testGetThreadStackNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            threadInsight.getThreadStack(null);
        });
    }
    
    @Test
    void testGetThreadCpuTime() {
        Thread currentThread = Thread.currentThread();
        long cpuTime = threadInsight.getThreadCpuTime(currentThread);
        
        // CPU time should be non-negative or -1 if not supported
        assertTrue(cpuTime >= -1, "CPU time should be >= -1");
        
        if (cpuTime > 0) {
            // Do some work
            long sum = 0;
            for (int i = 0; i < 1000000; i++) {
                sum += i;
            }
            
            long cpuTime2 = threadInsight.getThreadCpuTime(currentThread);
            assertTrue(cpuTime2 >= cpuTime, "CPU time should increase or stay same");
        }
    }
    
    @Test
    void testGetThreadCpuTimeNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            threadInsight.getThreadCpuTime(null);
        });
    }
    
    @Test
    void testGetThreadAllocatedBytes() {
        Thread currentThread = Thread.currentThread();
        long allocated1 = threadInsight.getThreadAllocatedBytes(currentThread);
        
        // Allocated bytes should be non-negative or -1 if not supported
        assertTrue(allocated1 >= -1, "Allocated bytes should be >= -1");
        
        if (allocated1 > 0) {
            // Allocate some objects
            Object[] objects = new Object[10000];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = new Object();
            }
            
            long allocated2 = threadInsight.getThreadAllocatedBytes(currentThread);
            assertTrue(allocated2 > allocated1, "Allocated bytes should increase after allocation");
        }
    }
    
    @Test
    void testGetThreadAllocatedBytesNullThrows() {
        assertThrows(NullPointerException.class, () -> {
            threadInsight.getThreadAllocatedBytes(null);
        });
    }
    
    @Test
    void testMultipleThreadsCpuTime() throws InterruptedException {
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                startLatch.countDown();
                try {
                    startLatch.await();
                    // Do enough CPU work to exceed clock-resolution granularity
                    // (100k iterations could finish within one tick on fast hosts)
                    long sum = 0;
                    for (int j = 0; j < 5_000_000; j++) {
                        sum += j * 31;
                    }
                    if (sum == 42L) {
                        throw new AssertionError();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
            threads[i].start();
        }
        
        endLatch.await(5, TimeUnit.SECONDS);
        
        // Check CPU time for all threads
        for (Thread thread : threads) {
            long cpuTime = threadInsight.getThreadCpuTime(thread);
            // Should have some CPU time or -1 if not supported
            assertTrue(cpuTime != 0 || cpuTime == -1, "Thread should have CPU time or return -1");
        }
        
        for (Thread thread : threads) {
            thread.join(1000);
        }
    }
    
    @Test
    void testGetThreadStackDepth() {
        // Create nested method calls to test stack depth
        StackTraceElement[] stack = nestedCall1();
        
        assertNotNull(stack);
        assertTrue(stack.length >= 3, "Should have at least 3 stack frames");
        
        // Verify method names in stack
        boolean found1 = false, found2 = false, found3 = false;
        for (StackTraceElement element : stack) {
            if (element.getMethodName().equals("nestedCall1")) found1 = true;
            if (element.getMethodName().equals("nestedCall2")) found2 = true;
            if (element.getMethodName().equals("nestedCall3")) found3 = true;
        }
        
        assertTrue(found1 && found2 && found3, "Should find all nested methods in stack");
    }
    
    private StackTraceElement[] nestedCall1() {
        return nestedCall2();
    }
    
    private StackTraceElement[] nestedCall2() {
        return nestedCall3();
    }
    
    private StackTraceElement[] nestedCall3() {
        return threadInsight.getThreadStack(Thread.currentThread());
    }
    
    @Test
    void testThreadStateTransitions() throws InterruptedException {
        CountDownLatch waitLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        
        Thread thread = new Thread(() -> {
            try {
                waitLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });
        
        thread.start();
        Thread.sleep(50); // Let thread start
        
        // Thread should be waiting
        StackTraceElement[] stack = threadInsight.getThreadStack(thread);
        assertNotNull(stack);
        
        waitLatch.countDown();
        finishLatch.await(1, TimeUnit.SECONDS);
        thread.join(1000);
    }
}
