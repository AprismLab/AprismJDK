package jdk.aprismate.profiler;

import java.nio.file.Path;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * ProfileResult - Results from a profiling session.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface ProfileResult {
    
    /**
     * Returns the profiling mode.
     * 
     * @return profiling mode
     */
    Profiler.ProfilingMode mode();
    
    /**
     * Returns the profiling duration.
     * 
     * @return duration
     */
    Duration duration();
    
    /**
     * Returns the total number of samples collected.
     * 
     * @return sample count
     */
    long totalSamples();
    
    /**
     * Returns the top N methods by sample count.
     * 
     * @param n the number of methods
     * @return top methods
     */
    List<MethodProfile> topMethods(int n);
    
    /**
     * Returns the top N allocation sites.
     * 
     * <p>Only available for allocation profiling.
     * 
     * @param n the number of allocations
     * @return top allocations
     * @throws UnsupportedOperationException if not allocation profiling
     */
    List<AllocationProfile> topAllocations(int n);
    
    /**
     * Returns the top N contended locks.
     * 
     * <p>Only available for lock profiling.
     * 
     * @param n the number of locks
     * @return top locks
     * @throws UnsupportedOperationException if not lock profiling
     */
    List<LockProfile> topLocks(int n);
    
    /**
     * Saves a flamegraph visualization as HTML.
     * 
     * @param path the output file path
     * @throws IOException if save fails
     */
    void saveFlamegraph(Path path) throws IOException;
    
    /**
     * Saves the raw profile data.
     * 
     * <p>Format depends on mode (JFR, collapsed stacks, etc.).
     * 
     * @param path the output file path
     * @throws IOException if save fails
     */
    void saveRaw(Path path) throws IOException;
    
    /**
     * Saves as JFR (Java Flight Recorder) format.
     * 
     * @param path the output file path
     * @throws IOException if save fails
     */
    void saveJfr(Path path) throws IOException;
    
    /**
     * Returns a textual summary.
     * 
     * @return summary string
     */
    String summary();
    
    /**
     * Method profile entry.
     */
    interface MethodProfile {
        
        /**
         * Returns the method name.
         * 
         * @return method name (format: "package.Class.method")
         */
        String name();
        
        /**
         * Returns the number of samples.
         * 
         * @return sample count
         */
        long samples();
        
        /**
         * Returns the percentage of total samples.
         * 
         * @return percentage (0-100)
         */
        double percentage();
        
        /**
         * Returns the stack trace for this method.
         * 
         * @return stack frames
         */
        List<StackFrame> stackTrace();
    }
    
    /**
     * Allocation profile entry.
     */
    interface AllocationProfile {
        
        /**
         * Returns the allocated type.
         * 
         * @return class name
         */
        String type();
        
        /**
         * Returns the allocation count.
         * 
         * @return allocation count
         */
        long count();
        
        /**
         * Returns the total bytes allocated.
         * 
         * @return bytes
         */
        long totalBytes();
        
        /**
         * Returns the percentage of total allocations.
         * 
         * @return percentage (0-100)
         */
        double percentage();
        
        /**
         * Returns the allocation site.
         * 
         * @return stack frames
         */
        List<StackFrame> allocationSite();
    }
    
    /**
     * Lock profile entry.
     */
    interface LockProfile {
        
        /**
         * Returns the monitor object.
         * 
         * @return monitor description
         */
        String monitor();
        
        /**
         * Returns the contention count.
         * 
         * @return contention count
         */
        long contentionCount();
        
        /**
         * Returns the total wait time.
         * 
         * @return total wait duration
         */
        Duration totalWaitTime();
        
        /**
         * Returns the average wait time.
         * 
         * @return average wait duration
         */
        Duration averageWaitTime();
        
        /**
         * Returns the maximum wait time.
         * 
         * @return max wait duration
         */
        Duration maxWaitTime();
        
        /**
         * Returns the lock acquisition site.
         * 
         * @return stack frames
         */
        List<StackFrame> lockSite();
    }
    
    /**
     * Stack frame representation.
     */
    interface StackFrame {
        
        /**
         * Returns the class name.
         * 
         * @return class name
         */
        String className();
        
        /**
         * Returns the method name.
         * 
         * @return method name
         */
        String methodName();
        
        /**
         * Returns the file name.
         * 
         * @return file name, or null if unavailable
         */
        String fileName();
        
        /**
         * Returns the line number.
         * 
         * @return line number, or -1 if unavailable
         */
        int lineNumber();
        
        /**
         * Checks if this is a native frame.
         * 
         * @return true if native
         */
        boolean isNative();
        
        /**
         * Returns the bytecode index.
         * 
         * @return BCI, or -1 if unavailable
         */
        int bci();
        
        /**
         * Returns a formatted string.
         * 
         * <p>Format: "package.Class.method(File.java:123)"
         * 
         * @return formatted string
         */
        default String format() {
            StringBuilder sb = new StringBuilder();
            sb.append(className()).append('.').append(methodName());
            if (fileName() != null) {
                sb.append('(').append(fileName());
                if (lineNumber() > 0) {
                    sb.append(':').append(lineNumber());
                }
                sb.append(')');
            } else if (isNative()) {
                sb.append("(Native Method)");
            }
            return sb.toString();
        }
    }
}
