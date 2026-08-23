package jdk.aprismate.concurrent;

import java.time.Duration;
import java.util.concurrent.Future;

/**
 * Fiber - Handle for a lightweight cooperative task.
 * 
 * <p>A fiber represents a single unit of execution managed by the
 * {@link FiberScheduler}. It provides methods to query status,
 * cancel execution, and wait for completion.
 * 
 * <h2>Lifecycle</h2>
 * <pre>
 * NEW -> RUNNABLE -> RUNNING -> COMPLETED
 *         v           v
 *      BLOCKED    CANCELLED
 * </pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface Fiber {
    
    /**
     * Returns the unique fiber ID.
     * 
     * @return fiber ID
     */
    long id();
    
    /**
     * Returns the fiber name, if set.
     * 
     * @return fiber name, or null if not set
     */
    String name();
    
    /**
     * Returns the current state of this fiber.
     * 
     * @return fiber state
     */
    State state();
    
    /**
     * Cancels this fiber's execution.
     * 
     * <p>This is a cooperative cancellation - the fiber must check
     * for cancellation and respond appropriately.
     * 
     * @param mayInterruptIfRunning whether to interrupt if running
     * @return true if cancelled, false if already completed
     */
    boolean cancel(boolean mayInterruptIfRunning);
    
    /**
     * Returns true if this fiber was cancelled.
     * 
     * @return true if cancelled
     */
    boolean isCancelled();
    
    /**
     * Returns true if this fiber has completed.
     * 
     * @return true if completed
     */
    boolean isDone();
    
    /**
     * Waits for this fiber to complete.
     * 
     * @throws InterruptedException if interrupted while waiting
     */
    void join() throws InterruptedException;
    
    /**
     * Waits for this fiber to complete with timeout.
     * 
     * @param timeout the maximum time to wait
     * @return true if completed, false if timeout elapsed
     * @throws InterruptedException if interrupted while waiting
     */
    boolean join(Duration timeout) throws InterruptedException;
    
    /**
     * Returns the carrier thread currently executing this fiber.
     * 
     * @return carrier thread, or null if not running
     */
    Thread carrierThread();
    
    /**
     * Yields execution to allow other fibers to run.
     * 
     * <p>This should be called periodically in long-running tasks
     * to ensure fair scheduling.
     */
    static void yield() {
        // Delegates to Thread.yield() or virtual thread yield
        Thread.yield();
    }
    
    /**
     * Returns the current fiber, or null if not running in a fiber.
     * 
     * @return current fiber or null
     */
    static Fiber current() {
        return FiberSchedulerFactory.currentFiber();
    }
    
    /**
     * Fiber state enumeration.
     */
    enum State {
        /** Created but not yet scheduled. */
        NEW,
        
        /** In scheduler queue, ready to run. */
        RUNNABLE,
        
        /** Currently executing on a carrier thread. */
        RUNNING,
        
        /** Blocked waiting for I/O or lock. */
        BLOCKED,
        
        /** Cancelled before completion. */
        CANCELLED,
        
        /** Execution completed normally or exceptionally. */
        COMPLETED
    }
}
