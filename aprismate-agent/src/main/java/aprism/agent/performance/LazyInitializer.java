package aprism.agent.performance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Lazy initialization manager for agent components.
 * <p>
 * This reduces agent startup time by deferring initialization of non-critical
 * components until they are first accessed.
 * </p>
 *
 * @since v26.1-Alpha.7
 */
public class LazyInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(LazyInitializer.class.getName());
    private static final ConcurrentHashMap<String, LazyHolder<?>> HOLDERS = new ConcurrentHashMap<>();
    
    /**
     * Registers a lazy component.
     *
     * @param name the component name
     * @param initializer the initialization function
     * @param <T> the component type
     */
    public static <T> void register(String name, Supplier<T> initializer) {
        HOLDERS.putIfAbsent(name, new LazyHolder<>(name, initializer));
    }
    
    /**
     * Gets or initializes a component.
     *
     * @param name the component name
     * @param type the expected type
     * @param <T> the component type
     * @return the initialized component
     * @throws IllegalStateException if component not registered or type mismatch
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name, Class<T> type) {
        LazyHolder<?> holder = HOLDERS.get(name);
        if (holder == null) {
            throw new IllegalStateException("Component not registered: " + name);
        }
        
        Object instance = holder.get();
        if (!type.isInstance(instance)) {
            throw new IllegalStateException("Type mismatch for component " + name + 
                ": expected " + type + ", got " + instance.getClass());
        }
        
        return (T) instance;
    }
    
    /**
     * Checks if a component is already initialized.
     *
     * @param name the component name
     * @return true if initialized
     */
    public static boolean isInitialized(String name) {
        LazyHolder<?> holder = HOLDERS.get(name);
        return holder != null && holder.isInitialized();
    }
    
    /**
     * Clears all lazy components (for testing).
     */
    public static void clear() {
        HOLDERS.clear();
    }
    
    /**
     * Holder for lazy-initialized component.
     */
    private static class LazyHolder<T> {
        private final String name;
        private final Supplier<T> initializer;
        private final AtomicBoolean initialized = new AtomicBoolean(false);
        private volatile T instance;
        
        LazyHolder(String name, Supplier<T> initializer) {
            this.name = name;
            this.initializer = initializer;
        }
        
        T get() {
            if (!initialized.get()) {
                synchronized (this) {
                    if (!initialized.get()) {
                        long start = System.nanoTime();
                        instance = initializer.get();
                        long elapsed = (System.nanoTime() - start) / 1_000_000;
                        LOGGER.fine("Initialized component '" + name + "' in " + elapsed + "ms");
                        initialized.set(true);
                    }
                }
            }
            return instance;
        }
        
        boolean isInitialized() {
            return initialized.get();
        }
    }
}
