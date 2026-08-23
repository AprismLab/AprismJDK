package jdk.aprismate.concurrent;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * Factory for creating FiberScheduler instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class FiberSchedulerFactory {
    
    private FiberSchedulerFactory() {
        // No instantiation
    }
    
    /**
     * Creates a new builder.
     */
    static FiberScheduler.Builder builder() {
        return new BuilderImpl();
    }
    
    /**
     * Returns the current fiber, or null if not in a fiber context.
     */
    static Fiber currentFiber() {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.concurrent.FiberImpl");
            return (Fiber) implClass.getMethod("current").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Builder implementation.
     */
    private static class BuilderImpl implements FiberScheduler.Builder {
        
        private int carrierThreads = Runtime.getRuntime().availableProcessors();
        private boolean workStealing = true;
        private String namePrefix = "fiber-scheduler";
        private int priority = Thread.NORM_PRIORITY;
        private boolean enableStats = true;
        
        @Override
        public FiberScheduler.Builder carrierThreads(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("carrierThreads must be positive: " + count);
            }
            this.carrierThreads = count;
            return this;
        }
        
        @Override
        public FiberScheduler.Builder enableWorkStealing(boolean enable) {
            this.workStealing = enable;
            return this;
        }
        
        @Override
        public FiberScheduler.Builder namePrefix(String prefix) {
            this.namePrefix = Objects.requireNonNull(prefix, "namePrefix");
            return this;
        }
        
        @Override
        public FiberScheduler.Builder carrierPriority(int priority) {
            if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
                throw new IllegalArgumentException(
                    "priority must be between " + Thread.MIN_PRIORITY + 
                    " and " + Thread.MAX_PRIORITY + ": " + priority);
            }
            this.priority = priority;
            return this;
        }
        
        @Override
        public FiberScheduler.Builder enableStats(boolean enable) {
            this.enableStats = enable;
            return this;
        }
        
        @Override
        public FiberScheduler build() {
            try {
                // Try to use agent implementation
                Class<?> implClass = Class.forName("com.aprismate.agent.concurrent.FiberSchedulerImpl");
                return (FiberScheduler) implClass.getConstructor(
                    int.class, boolean.class, String.class, int.class, boolean.class
                ).newInstance(carrierThreads, workStealing, namePrefix, priority, enableStats);
            } catch (Exception e) {
                // Fall back to stub implementation
                return new StubFiberScheduler(carrierThreads, namePrefix);
            }
        }
    }
    
    /**
     * Stub implementation using ForkJoinPool.
     */
    private static class StubFiberScheduler implements FiberScheduler {
        
        private final ExecutorService executor;
        private final int carrierThreads;
        
        StubFiberScheduler(int carrierThreads, String namePrefix) {
            this.carrierThreads = carrierThreads;
            this.executor = new ForkJoinPool(carrierThreads);
        }
        
        @Override
        public Fiber schedule(Runnable task) {
            Objects.requireNonNull(task, "task");
            Future<?> future = executor.submit(task);
            return new StubFiber(future);
        }
        
        @Override
        public <T> Future<T> schedule(Callable<T> task) {
            Objects.requireNonNull(task, "task");
            return executor.submit(task);
        }
        
        @Override
        public Fiber scheduleWithDelay(Runnable task, Duration delay) {
            Objects.requireNonNull(task, "task");
            Objects.requireNonNull(delay, "delay");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ScheduledFuture<?> future = scheduler.schedule(
                task, delay.toNanos(), TimeUnit.NANOSECONDS);
            return new StubFiber(future);
        }
        
        @Override
        public Fiber scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
            Objects.requireNonNull(task, "task");
            Objects.requireNonNull(initialDelay, "initialDelay");
            Objects.requireNonNull(period, "period");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                task, initialDelay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
            return new StubFiber(future);
        }
        
        @Override
        public int carrierThreads() {
            return carrierThreads;
        }
        
        @Override
        public long activeFibers() {
            return 0;
        }
        
        @Override
        public long queuedFibers() {
            return 0;
        }
        
        @Override
        public SchedulerStats stats() {
            return new StubSchedulerStats(carrierThreads);
        }
        
        @Override
        public void shutdown() {
            executor.shutdown();
        }
        
        @Override
        public List<Runnable> shutdownNow() {
            return executor.shutdownNow();
        }
        
        @Override
        public boolean isShutdown() {
            return executor.isShutdown();
        }
        
        @Override
        public boolean isTerminated() {
            return executor.isTerminated();
        }
        
        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return executor.awaitTermination(timeout, unit);
        }
        
        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return schedule(task);
        }
        
        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return executor.submit(task, result);
        }
        
        @Override
        public Future<?> submit(Runnable task) {
            schedule(task);
            return executor.submit(task);
        }
        
        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return executor.invokeAll(tasks);
        }
        
        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return executor.invokeAll(tasks, timeout, unit);
        }
        
        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return executor.invokeAny(tasks);
        }
        
        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return executor.invokeAny(tasks, timeout, unit);
        }
        
        @Override
        public void execute(Runnable command) {
            schedule(command);
        }
    }
    
    /**
     * Stub Fiber implementation.
     */
    private static class StubFiber implements Fiber {
        
        private static long nextId = 1;
        private final long id = nextId++;
        private final Future<?> future;
        
        StubFiber(Future<?> future) {
            this.future = future;
        }
        
        @Override
        public long id() {
            return id;
        }
        
        @Override
        public String name() {
            return "fiber-" + id;
        }
        
        @Override
        public State state() {
            if (future.isCancelled()) {
                return State.CANCELLED;
            }
            if (future.isDone()) {
                return State.COMPLETED;
            }
            return State.RUNNING;
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }
        
        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }
        
        @Override
        public boolean isDone() {
            return future.isDone();
        }
        
        @Override
        public void join() throws InterruptedException {
            try {
                future.get();
            } catch (ExecutionException e) {
                // Ignore
            }
        }
        
        @Override
        public boolean join(Duration timeout) throws InterruptedException {
            try {
                future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
                return true;
            } catch (ExecutionException | TimeoutException e) {
                return false;
            }
        }
        
        @Override
        public Thread carrierThread() {
            return null;
        }
    }
    
    /**
     * Stub SchedulerStats implementation.
     */
    private static class StubSchedulerStats implements SchedulerStats {
        
        private final int carrierThreads;
        private final long startTime = System.currentTimeMillis();
        
        StubSchedulerStats(int carrierThreads) {
            this.carrierThreads = carrierThreads;
        }
        
        @Override
        public int carrierThreads() {
            return carrierThreads;
        }
        
        @Override
        public int activeCarriers() {
            return 0;
        }
        
        @Override
        public long totalFibers() {
            return 0;
        }
        
        @Override
        public long activeFibers() {
            return 0;
        }
        
        @Override
        public long queuedFibers() {
            return 0;
        }
        
        @Override
        public long completedFibers() {
            return 0;
        }
        
        @Override
        public long cancelledFibers() {
            return 0;
        }
        
        @Override
        public long failedFibers() {
            return 0;
        }
        
        @Override
        public long peakActiveFibers() {
            return 0;
        }
        
        @Override
        public long averageExecutionTime() {
            return 0;
        }
        
        @Override
        public long averageWaitTime() {
            return 0;
        }
        
        @Override
        public long contextSwitches() {
            return 0;
        }
        
        @Override
        public long workSteals() {
            return 0;
        }
        
        @Override
        public long failedSteals() {
            return 0;
        }
        
        @Override
        public double throughput() {
            return 0.0;
        }
        
        @Override
        public long averageContextSwitchTime() {
            return 0;
        }
        
        @Override
        public void reset() {
            // No-op
        }
        
        @Override
        public long startTime() {
            return startTime;
        }
    }
}
