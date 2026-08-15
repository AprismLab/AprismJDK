package jdk.aprismate.concurrent;

import java.time.Duration;
import java.util.concurrent.*;

/**
 * FiberScheduler - Lightweight cooperative scheduler for virtual threads (fibers).
 * 
 * <p>This scheduler manages millions of fibers with minimal overhead using
 * work-stealing and M:N threading. Unlike traditional threads (1:1 mapping
 * to OS threads), fibers are user-space constructs that are multiplexed onto
 * a small pool of carrier threads.
 * 
 * <h2>Benefits over Platform Threads</h2>
 * <ul>
 *   <li><b>Scale</b>: Millions of fibers vs thousands of threads</li>
 *   <li><b>Memory</b>: ~1KB per fiber vs ~1MB per thread</li>
 *   <li><b>Creation</b>: 1-2μs vs 100-500μs</li>
 *   <li><b>Context switch</b>: 100ns vs 10μs</li>
 * </ul>
 * 
 * <h2>Architecture</h2>
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │        Fiber Scheduler (M:N)            │
 * ├─────────────────────────────────────────┤
 * │ Fiber1  Fiber2  Fiber3  ...  FiberN     │ (Millions)
 * │   ↓       ↓       ↓            ↓        │
 * │ [Work-Stealing Queues]                  │
 * │   ↓       ↓       ↓            ↓        │
 * │ Thread1 Thread2 Thread3 ... ThreadM     │ (Few, typically CPU count)
 * └─────────────────────────────────────────┘
 * </pre>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * FiberScheduler scheduler = FiberScheduler.builder()
 *     .carrierThreads(Runtime.getRuntime().availableProcessors())
 *     .enableWorkStealing(true)
 *     .build();
 * 
 * // Launch 1 million concurrent tasks
 * for (int i = 0; i < 1_000_000; i++) {
 *     scheduler.schedule(() -> {
 *         // Lightweight cooperative task
 *         String result = fetchData();
 *         processData(result);
 *     });
 * }
 * 
 * // Wait for completion
 * scheduler.awaitTermination(1, TimeUnit.HOURS);
 * scheduler.shutdown();
 * 
 * // Check statistics
 * SchedulerStats stats = scheduler.stats();
 * System.out.println("Active fibers: " + stats.activeFibers());
 * System.out.println("Completed: " + stats.completedFibers());
 * }</pre>
 * 
 * <h2>Cooperative Scheduling</h2>
 * <p>Fibers must cooperate by yielding at appropriate points:
 * <pre>{@code
 * void longRunningTask() {
 *     for (int i = 0; i < 1000000; i++) {
 *         compute();
 *         
 *         // Yield every 10000 iterations
 *         if (i % 10000 == 0) {
 *             Fiber.yield();
 *         }
 *     }
 * }
 * }</pre>
 * 
 * <h2>Integration with Java 21+ Virtual Threads</h2>
 * <p>On Java 21+, this delegates to the JVM's native virtual thread
 * implementation. On earlier versions, it provides a compatible implementation.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface FiberScheduler extends ExecutorService {
    
    /**
     * Schedules a task to run on a fiber.
     * 
     * <p>The task is added to the scheduler's work queue and will be
     * executed when a carrier thread becomes available. This method
     * returns immediately without blocking.
     * 
     * @param task the task to execute
     * @return a Fiber handle for the scheduled task
     * @throws RejectedExecutionException if the scheduler is shut down
     * @throws NullPointerException if task is null
     */
    Fiber schedule(Runnable task);
    
    /**
     * Schedules a task with a result.
     * 
     * @param <T> the result type
     * @param task the task to execute
     * @return a Future representing the pending result
     * @throws RejectedExecutionException if the scheduler is shut down
     * @throws NullPointerException if task is null
     */
    <T> Future<T> schedule(Callable<T> task);
    
    /**
     * Schedules a task with a delay.
     * 
     * @param task the task to execute
     * @param delay the delay duration
     * @return a Fiber handle for the scheduled task
     * @throws RejectedExecutionException if the scheduler is shut down
     * @throws NullPointerException if task or delay is null
     */
    Fiber scheduleWithDelay(Runnable task, Duration delay);
    
    /**
     * Schedules a periodic task.
     * 
     * <p>The task is executed repeatedly with a fixed delay between
     * the end of one execution and the start of the next.
     * 
     * @param task the task to execute
     * @param initialDelay the initial delay
     * @param period the period between executions
     * @return a Fiber handle that can be used to cancel the task
     * @throws RejectedExecutionException if the scheduler is shut down
     * @throws NullPointerException if any parameter is null
     */
    Fiber scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period);
    
    /**
     * Returns the number of carrier threads.
     * 
     * <p>These are the underlying platform threads that execute fibers.
     * 
     * @return carrier thread count
     */
    int carrierThreads();
    
    /**
     * Returns the number of currently active fibers.
     * 
     * <p>This includes fibers that are running or waiting in queues.
     * 
     * @return active fiber count
     */
    long activeFibers();
    
    /**
     * Returns the number of queued fibers waiting to execute.
     * 
     * @return queued fiber count
     */
    long queuedFibers();
    
    /**
     * Returns scheduler statistics.
     * 
     * @return current statistics
     */
    SchedulerStats stats();
    
    /**
     * Initiates an orderly shutdown.
     * 
     * <p>Previously submitted tasks are executed, but no new tasks
     * will be accepted. This method does not wait for tasks to complete.
     */
    @Override
    void shutdown();
    
    /**
     * Attempts to stop all actively executing tasks.
     * 
     * <p>This is a best-effort attempt. There are no guarantees
     * that tasks will actually stop.
     * 
     * @return list of tasks that never commenced execution
     */
    @Override
    java.util.List<Runnable> shutdownNow();
    
    /**
     * Returns true if this scheduler has been shut down.
     * 
     * @return true if shut down
     */
    @Override
    boolean isShutdown();
    
    /**
     * Returns true if all tasks have completed after shutdown.
     * 
     * @return true if terminated
     */
    @Override
    boolean isTerminated();
    
    /**
     * Blocks until all tasks complete or timeout occurs.
     * 
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout
     * @return true if terminated, false if timeout elapsed
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
    
    /**
     * Creates a new scheduler builder.
     * 
     * @return a new builder instance
     */
    static Builder builder() {
        return FiberSchedulerFactory.builder();
    }
    
    /**
     * Creates a scheduler with default configuration.
     * 
     * <p>Uses available processor count as carrier thread count
     * and enables work stealing.
     * 
     * @return a new scheduler
     */
    static FiberScheduler create() {
        return builder().build();
    }
    
    /**
     * Builder for FiberScheduler.
     */
    interface Builder {
        
        /**
         * Sets the number of carrier threads.
         * 
         * <p>Default is {@code Runtime.getRuntime().availableProcessors()}.
         * 
         * @param count the number of carrier threads
         * @return this builder
         * @throws IllegalArgumentException if count is not positive
         */
        Builder carrierThreads(int count);
        
        /**
         * Enables or disables work stealing.
         * 
         * <p>When enabled, idle carrier threads steal work from busy threads.
         * This improves load balancing but adds overhead. Default is true.
         * 
         * @param enable true to enable, false to disable
         * @return this builder
         */
        Builder enableWorkStealing(boolean enable);
        
        /**
         * Sets the scheduler name prefix.
         * 
         * <p>Carrier threads will be named "{prefix}-carrier-{n}".
         * 
         * @param prefix the name prefix
         * @return this builder
         * @throws NullPointerException if prefix is null
         */
        Builder namePrefix(String prefix);
        
        /**
         * Sets the thread priority for carrier threads.
         * 
         * <p>Must be between {@link Thread#MIN_PRIORITY} and {@link Thread#MAX_PRIORITY}.
         * 
         * @param priority the priority
         * @return this builder
         * @throws IllegalArgumentException if priority is out of range
         */
        Builder carrierPriority(int priority);
        
        /**
         * Enables or disables statistics collection.
         * 
         * <p>When disabled, stats methods return zero values. Default is true.
         * 
         * @param enable true to enable, false to disable
         * @return this builder
         */
        Builder enableStats(boolean enable);
        
        /**
         * Builds the scheduler.
         * 
         * @return a new FiberScheduler instance
         */
        FiberScheduler build();
    }
}
