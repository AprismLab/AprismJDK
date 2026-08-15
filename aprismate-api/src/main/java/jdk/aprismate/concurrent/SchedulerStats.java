package jdk.aprismate.concurrent;

/**
 * SchedulerStats - Statistics for FiberScheduler.
 * 
 * <p>Provides detailed metrics about scheduler performance, fiber lifecycle,
 * and resource utilization.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface SchedulerStats {
    
    /**
     * Returns the number of carrier threads.
     * 
     * @return carrier thread count
     */
    int carrierThreads();
    
    /**
     * Returns the number of active carrier threads.
     * 
     * <p>A carrier thread is active if it is currently executing a fiber.
     * 
     * @return active carrier count
     */
    int activeCarriers();
    
    /**
     * Returns the number of idle carrier threads.
     * 
     * @return idle carrier count
     */
    default int idleCarriers() {
        return carrierThreads() - activeCarriers();
    }
    
    /**
     * Returns the total number of fibers created.
     * 
     * @return total fibers created
     */
    long totalFibers();
    
    /**
     * Returns the number of currently active fibers.
     * 
     * <p>Active fibers are those in RUNNABLE or RUNNING state.
     * 
     * @return active fiber count
     */
    long activeFibers();
    
    /**
     * Returns the number of fibers waiting in queues.
     * 
     * @return queued fiber count
     */
    long queuedFibers();
    
    /**
     * Returns the number of completed fibers.
     * 
     * @return completed fiber count
     */
    long completedFibers();
    
    /**
     * Returns the number of cancelled fibers.
     * 
     * @return cancelled fiber count
     */
    long cancelledFibers();
    
    /**
     * Returns the number of fibers that failed with exception.
     * 
     * @return failed fiber count
     */
    long failedFibers();
    
    /**
     * Returns the peak number of active fibers.
     * 
     * @return peak active fibers
     */
    long peakActiveFibers();
    
    /**
     * Returns the average fiber execution time in nanoseconds.
     * 
     * @return average execution time in ns
     */
    long averageExecutionTime();
    
    /**
     * Returns the average time fibers spend waiting in queue.
     * 
     * @return average wait time in ns
     */
    long averageWaitTime();
    
    /**
     * Returns the total number of fiber context switches.
     * 
     * @return total context switches
     */
    long contextSwitches();
    
    /**
     * Returns the number of work-stealing operations.
     * 
     * <p>This is the number of times an idle carrier stole work
     * from another carrier's queue.
     * 
     * @return work steals
     */
    long workSteals();
    
    /**
     * Returns the number of failed work-stealing attempts.
     * 
     * @return failed steals
     */
    long failedSteals();
    
    /**
     * Calculates the work-stealing success rate.
     * 
     * @return success rate percentage (0-100)
     */
    default double stealSuccessRate() {
        long total = workSteals() + failedSteals();
        if (total == 0) {
            return 0.0;
        }
        return (workSteals() * 100.0) / total;
    }
    
    /**
     * Calculates the average number of fibers per carrier.
     * 
     * @return average fibers per carrier
     */
    default double averageFibersPerCarrier() {
        int carriers = carrierThreads();
        if (carriers == 0) {
            return 0.0;
        }
        return (double) activeFibers() / carriers;
    }
    
    /**
     * Calculates the carrier utilization percentage.
     * 
     * <p>Utilization = (active carriers / total carriers) * 100
     * 
     * @return utilization percentage (0-100)
     */
    default double carrierUtilization() {
        int total = carrierThreads();
        if (total == 0) {
            return 0.0;
        }
        return (activeCarriers() * 100.0) / total;
    }
    
    /**
     * Calculates the throughput in fibers completed per second.
     * 
     * @return throughput in fibers/s
     */
    double throughput();
    
    /**
     * Returns the average context switch time in nanoseconds.
     * 
     * @return average switch time in ns
     */
    long averageContextSwitchTime();
    
    /**
     * Resets all statistics to zero.
     */
    void reset();
    
    /**
     * Returns the time when statistics collection started.
     * 
     * @return start time in milliseconds since epoch
     */
    long startTime();
    
    /**
     * Returns the elapsed time since statistics collection started.
     * 
     * @return elapsed time in milliseconds
     */
    default long elapsedTime() {
        return System.currentTimeMillis() - startTime();
    }
}
