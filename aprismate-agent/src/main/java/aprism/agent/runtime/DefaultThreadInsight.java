package aprism.agent.runtime;

import jdk.aprismate.runtime.ThreadInsight;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Default implementation of ThreadInsight using ThreadMXBean.
 *
 * @since v26.1-Alpha.6
 */
public class DefaultThreadInsight implements ThreadInsight {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultThreadInsight.class.getName());
    private final ThreadMXBean threadMXBean;
    private final boolean cpuTimeSupported;
    private final boolean allocatedBytesSupported;
    
    public DefaultThreadInsight() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.cpuTimeSupported = threadMXBean.isThreadCpuTimeSupported();
        
        // Check if thread allocated bytes is supported (Java 14+)
        boolean allocSupported = false;
        try {
            java.lang.management.ThreadMXBean bean = threadMXBean;
            // Try to find getAllocatedBytes method
            bean.getClass().getMethod("getThreadAllocatedBytes", long.class);
            allocSupported = true;
        } catch (NoSuchMethodException e) {
            LOGGER.info("Thread allocated bytes tracking not supported on this JVM");
        }
        this.allocatedBytesSupported = allocSupported;
        
        // Enable CPU time measurement if supported
        if (cpuTimeSupported && !threadMXBean.isThreadCpuTimeEnabled()) {
            threadMXBean.setThreadCpuTimeEnabled(true);
        }
    }
    
    @Override
    public List<Thread> getAllThreads() {
        ThreadGroup rootGroup = getRootThreadGroup();
        
        // Estimate thread count and allocate array
        int estimatedSize = rootGroup.activeCount() * 2;
        Thread[] threads = new Thread[estimatedSize];
        
        int actualSize = rootGroup.enumerate(threads, true);
        
        // If we underestimated, try again with larger array
        if (actualSize == threads.length) {
            threads = new Thread[actualSize * 2];
            actualSize = rootGroup.enumerate(threads, true);
        }
        
        List<Thread> result = new ArrayList<>(actualSize);
        for (int i = 0; i < actualSize; i++) {
            if (threads[i] != null) {
                result.add(threads[i]);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    @Override
    public StackTraceElement[] getThreadStack(Thread thread) {
        if (thread == null) {
            throw new NullPointerException("thread cannot be null");
        }
        
        // Use ThreadMXBean for more accurate stack traces
        long threadId = thread.threadId();
        java.lang.management.ThreadInfo info = threadMXBean.getThreadInfo(threadId, Integer.MAX_VALUE);
        
        if (info == null) {
            // Thread may have died, return empty array
            return new StackTraceElement[0];
        }
        
        return info.getStackTrace();
    }
    
    @Override
    public long getThreadCpuTime(Thread thread) {
        if (thread == null) {
            throw new NullPointerException("thread cannot be null");
        }
        
        if (!cpuTimeSupported) {
            return -1;
        }
        
        try {
            long threadId = thread.threadId();
            return threadMXBean.getThreadCpuTime(threadId);
        } catch (Exception e) {
            LOGGER.warning("Failed to get CPU time for thread " + thread.getName() + ": " + e.getMessage());
            return -1;
        }
    }
    
    @Override
    public long getThreadAllocatedBytes(Thread thread) {
        if (thread == null) {
            throw new NullPointerException("thread cannot be null");
        }
        
        if (!allocatedBytesSupported) {
            return -1;
        }
        
        try {
            long threadId = thread.threadId();
            // Use reflection to call getThreadAllocatedBytes (Java 14+)
            java.lang.reflect.Method method = threadMXBean.getClass()
                .getMethod("getThreadAllocatedBytes", long.class);
            return (Long) method.invoke(threadMXBean, threadId);
        } catch (Exception e) {
            LOGGER.fine("Failed to get allocated bytes for thread " + thread.getName() + ": " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Gets the root thread group.
     */
    private ThreadGroup getRootThreadGroup() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        
        return group;
    }
}
