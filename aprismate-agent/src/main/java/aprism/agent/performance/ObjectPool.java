package aprism.agent.performance;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Lightweight object pool for reducing allocation overhead.
 * <p>
 * This reduces memory pressure and GC overhead for frequently allocated
 * objects in hot paths.
 * </p>
 *
 * @param <T> the pooled object type
 * @since v26.1-Alpha.7
 */
public class ObjectPool<T> {
    
    private static final Logger LOGGER = Logger.getLogger(ObjectPool.class.getName());
    private final BlockingQueue<T> pool;
    private final Factory<T> factory;
    private final int maxSize;
    private int created = 0;
    
    /**
     * Factory for creating pooled objects.
     */
    @FunctionalInterface
    public interface Factory<T> {
        T create();
    }
    
    /**
     * Creates an object pool.
     *
     * @param factory the object factory
     * @param maxSize maximum pool size
     */
    public ObjectPool(Factory<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ArrayBlockingQueue<>(maxSize);
    }
    
    /**
     * Acquires an object from the pool.
     * <p>
     * If the pool is empty, creates a new object.
     * </p>
     *
     * @return a pooled object
     */
    public T acquire() {
        T obj = pool.poll();
        if (obj == null) {
            synchronized (this) {
                if (created < maxSize) {
                    obj = factory.create();
                    created++;
                } else {
                    // Pool exhausted, create temporary object
                    obj = factory.create();
                    LOGGER.fine("Object pool exhausted, created temporary object");
                }
            }
        }
        return obj;
    }
    
    /**
     * Acquires an object with timeout.
     *
     * @param timeout the timeout
     * @param unit the time unit
     * @return a pooled object, or null if timeout
     * @throws InterruptedException if interrupted
     */
    public T acquire(long timeout, TimeUnit unit) throws InterruptedException {
        T obj = pool.poll(timeout, unit);
        if (obj == null) {
            synchronized (this) {
                if (created < maxSize) {
                    obj = factory.create();
                    created++;
                }
            }
        }
        return obj;
    }
    
    /**
     * Returns an object to the pool.
     * <p>
     * If the pool is full, the object is discarded.
     * </p>
     *
     * @param obj the object to return
     */
    public void release(T obj) {
        if (obj != null && !pool.offer(obj)) {
            // Pool is full, discard object
            LOGGER.fine("Object pool full, discarding object");
        }
    }
    
    /**
     * Returns the current pool size.
     *
     * @return pool size
     */
    public int size() {
        return pool.size();
    }
    
    /**
     * Returns the total number of objects created.
     *
     * @return created count
     */
    public int getCreatedCount() {
        return created;
    }
    
    /**
     * Clears the pool.
     */
    public void clear() {
        pool.clear();
    }
}
