package aprism.agent.performance;

/**
 * Fast thread-local cache for reducing synchronization overhead.
 * <p>
 * This provides thread-local storage with minimal overhead, useful for
 * caching frequently accessed data in hot paths.
 * </p>
 *
 * @param <T> the cached value type
 * @since v26.1-Alpha.7
 */
public class FastThreadLocal<T> {
    
    private final ThreadLocal<T> threadLocal;
    
    /**
     * Factory for creating initial values.
     */
    @FunctionalInterface
    public interface Supplier<T> {
        T get();
    }
    
    /**
     * Creates a fast thread-local with a supplier.
     *
     * @param supplier the initial value supplier
     */
    public FastThreadLocal(Supplier<T> supplier) {
        this.threadLocal = ThreadLocal.withInitial(supplier::get);
    }
    
    /**
     * Creates a fast thread-local with null initial value.
     */
    public FastThreadLocal() {
        this.threadLocal = new ThreadLocal<>();
    }
    
    /**
     * Gets the current thread's value.
     *
     * @return the value
     */
    public T get() {
        return threadLocal.get();
    }
    
    /**
     * Sets the current thread's value.
     *
     * @param value the new value
     */
    public void set(T value) {
        threadLocal.set(value);
    }
    
    /**
     * Removes the current thread's value.
     */
    public void remove() {
        threadLocal.remove();
    }
    
    /**
     * Gets the value, initializing if absent.
     *
     * @param initializer the initializer function
     * @return the value
     */
    public T getOrCompute(Supplier<T> initializer) {
        T value = threadLocal.get();
        if (value == null) {
            value = initializer.get();
            threadLocal.set(value);
        }
        return value;
    }
}
