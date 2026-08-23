package jdk.aprismate.concurrent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Factory for creating LockFreeQueue instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class LockFreeQueueFactory {
    
    private LockFreeQueueFactory() {
        // No instantiation
    }
    
    /**
     * Creates an unbounded lock-free queue.
     */
    @SuppressWarnings("unchecked")
    static <E> LockFreeQueue<E> createUnbounded() {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.concurrent.UnboundedLockFreeQueue");
            return (LockFreeQueue<E>) implClass.getMethod("create").invoke(null);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubLockFreeQueue<>();
        }
    }
    
    /**
     * Creates a bounded lock-free queue.
     */
    @SuppressWarnings("unchecked")
    static <E> LockFreeQueue<E> createBounded(int capacity) {
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.concurrent.BoundedLockFreeQueue");
            return (LockFreeQueue<E>) implClass.getMethod("create", int.class).invoke(null, capacity);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubLockFreeQueue<>(capacity);
        }
    }
    
    /**
     * Stub implementation using ConcurrentLinkedQueue.
     */
    private static class StubLockFreeQueue<E> implements LockFreeQueue<E> {
        
        private final Queue<E> backing = new ConcurrentLinkedQueue<>();
        private final int capacity;
        
        StubLockFreeQueue() {
            this.capacity = Integer.MAX_VALUE;
        }
        
        StubLockFreeQueue(int capacity) {
            this.capacity = capacity;
        }
        
        @Override
        public boolean offer(E element) {
            Objects.requireNonNull(element, "element");
            if (capacity != Integer.MAX_VALUE && size() >= capacity) {
                return false;
            }
            return backing.offer(element);
        }
        
        @Override
        public boolean offer(E element, long timeout, TimeUnit unit) throws InterruptedException {
            return offer(element);
        }
        
        @Override
        public E poll() {
            return backing.poll();
        }
        
        @Override
        public E poll(long timeout, TimeUnit unit) throws InterruptedException {
            return poll();
        }
        
        @Override
        public int drainTo(Collection<? super E> collection) {
            Objects.requireNonNull(collection, "collection");
            int count = 0;
            E element;
            while ((element = poll()) != null) {
                collection.add(element);
                count++;
            }
            return count;
        }
        
        @Override
        public int drainTo(Collection<? super E> collection, int maxElements) {
            Objects.requireNonNull(collection, "collection");
            if (maxElements < 0) {
                throw new IllegalArgumentException("maxElements must be non-negative: " + maxElements);
            }
            int count = 0;
            E element;
            while (count < maxElements && (element = poll()) != null) {
                collection.add(element);
                count++;
            }
            return count;
        }
        
        @Override
        public int remainingCapacity() {
            if (capacity == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return Math.max(0, capacity - size());
        }
        
        @Override
        public boolean isBounded() {
            return capacity != Integer.MAX_VALUE;
        }
        
        @Override
        public QueueStats stats() {
            return new StubQueueStats();
        }
        
        // Queue interface
        
        @Override
        public boolean add(E e) {
            if (offer(e)) {
                return true;
            }
            throw new IllegalStateException("Queue full");
        }
        
        @Override
        public E remove() {
            E element = poll();
            if (element == null) {
                throw new NoSuchElementException();
            }
            return element;
        }
        
        @Override
        public E element() {
            E element = peek();
            if (element == null) {
                throw new NoSuchElementException();
            }
            return element;
        }
        
        @Override
        public E peek() {
            return backing.peek();
        }
        
        @Override
        public int size() {
            return backing.size();
        }
        
        @Override
        public boolean isEmpty() {
            return backing.isEmpty();
        }
        
        @Override
        public boolean contains(Object o) {
            return backing.contains(o);
        }
        
        @Override
        public Iterator<E> iterator() {
            return backing.iterator();
        }
        
        @Override
        public Object[] toArray() {
            return backing.toArray();
        }
        
        @Override
        public <T> T[] toArray(T[] a) {
            return backing.toArray(a);
        }
        
        @Override
        public boolean remove(Object o) {
            return backing.remove(o);
        }
        
        @Override
        public boolean containsAll(Collection<?> c) {
            return backing.containsAll(c);
        }
        
        @Override
        public boolean addAll(Collection<? extends E> c) {
            for (E e : c) {
                if (!offer(e)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public boolean removeAll(Collection<?> c) {
            return backing.removeAll(c);
        }
        
        @Override
        public boolean retainAll(Collection<?> c) {
            return backing.retainAll(c);
        }
        
        @Override
        public void clear() {
            backing.clear();
        }
    }
    
    /**
     * Stub stats implementation.
     */
    private static class StubQueueStats implements QueueStats {
        
        private final long startTime = System.currentTimeMillis();
        
        @Override
        public long totalEnqueues() {
            return 0;
        }
        
        @Override
        public long totalDequeues() {
            return 0;
        }
        
        @Override
        public long failedEnqueues() {
            return 0;
        }
        
        @Override
        public long failedDequeues() {
            return 0;
        }
        
        @Override
        public int currentSize() {
            return 0;
        }
        
        @Override
        public int peakSize() {
            return 0;
        }
        
        @Override
        public double averageSize() {
            return 0.0;
        }
        
        @Override
        public double throughput() {
            return 0.0;
        }
        
        @Override
        public long averageEnqueueLatency() {
            return 0;
        }
        
        @Override
        public long averageDequeueLatency() {
            return 0;
        }
        
        @Override
        public long casRetries() {
            return 0;
        }
        
        @Override
        public double utilization() {
            return 0.0;
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
