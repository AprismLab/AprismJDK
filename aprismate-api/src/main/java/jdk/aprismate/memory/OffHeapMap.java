package jdk.aprismate.memory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * OffHeapMap - High-performance off-heap hash map for large datasets.
 * 
 * <p>This map stores data in native memory outside the Java heap, which
 * eliminates GC pressure and enables storing datasets larger than heap size.
 * It provides similar semantics to {@link java.util.HashMap} but with
 * off-heap storage.
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Zero GC overhead for map storage</li>
 *   <li>Support for datasets larger than heap size</li>
 *   <li>Primitive specializations to avoid boxing</li>
 *   <li>Persistent snapshots to disk</li>
 *   <li>Thread-safe operations</li>
 * </ul>
 * 
 * <h2>Use Cases</h2>
 * <ul>
 *   <li>Caching layers (Redis-like in-memory stores)</li>
 *   <li>Large lookup tables</li>
 *   <li>Session storage</li>
 *   <li>Real-time analytics aggregation</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * try (OffHeapMap<String, User> cache = OffHeapMap.create(
 *         String.class, User.class, 1_000_000)) {
 *     
 *     cache.put("user:1", new User("Alice"));
 *     User user = cache.get("user:1");
 *     
 *     // Primitive operations (no boxing)
 *     cache.putLong("counter", 42L);
 *     long count = cache.getLong("counter", 0L);
 *     
 *     // Batch operations
 *     Map<String, User> users = cache.getAll(userIds);
 *     
 *     // Persist to disk
 *     cache.snapshot(Path.of("cache.dat"));
 *     
 *     System.out.println("Memory: " + cache.memoryUsage() + " bytes");
 * }
 * }</pre>
 * 
 * <h2>Performance</h2>
 * <ul>
 *   <li>Put: O(1), ~100ns typical</li>
 *   <li>Get: O(1), ~80ns typical</li>
 *   <li>Memory overhead: ~20% for metadata</li>
 *   <li>Scales to billions of entries</li>
 * </ul>
 * 
 * @param <K> the key type
 * @param <V> the value type
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface OffHeapMap<K, V> extends Map<K, V>, AutoCloseable {
    
    /**
     * Gets a long value without boxing.
     * 
     * <p>This is more efficient than {@link #get(Object)} for primitive values
     * as it avoids object allocation.
     * 
     * @param key the key
     * @param defaultValue the value to return if key is not found
     * @return the value, or defaultValue if not found
     */
    long getLong(K key, long defaultValue);
    
    /**
     * Gets an int value without boxing.
     * 
     * @param key the key
     * @param defaultValue the value to return if key is not found
     * @return the value, or defaultValue if not found
     */
    int getInt(K key, int defaultValue);
    
    /**
     * Gets a double value without boxing.
     * 
     * @param key the key
     * @param defaultValue the value to return if key is not found
     * @return the value, or defaultValue if not found
     */
    double getDouble(K key, double defaultValue);
    
    /**
     * Puts a long value without boxing.
     * 
     * @param key the key
     * @param value the value
     */
    void putLong(K key, long value);
    
    /**
     * Puts an int value without boxing.
     * 
     * @param key the key
     * @param value the value
     */
    void putInt(K key, int value);
    
    /**
     * Puts a double value without boxing.
     * 
     * @param key the key
     * @param value the value
     */
    void putDouble(K key, double value);
    
    /**
     * Gets multiple values in a single operation.
     * 
     * <p>This is more efficient than calling {@link #get(Object)} repeatedly
     * as it can batch the operations.
     * 
     * @param keys the keys to retrieve
     * @return a map containing the found entries
     */
    Map<K, V> getAll(Collection<? extends K> keys);
    
    /**
     * Puts multiple entries in a single operation.
     * 
     * <p>This is more efficient than calling {@link #put(Object, Object)}
     * repeatedly as it can batch the operations.
     * 
     * @param map the entries to put
     */
    @Override
    void putAll(Map<? extends K, ? extends V> map);
    
    /**
     * Returns the total memory used by this map in bytes.
     * 
     * <p>This includes both data and metadata (hash table, collision chains, etc.).
     * 
     * @return memory usage in bytes
     */
    long memoryUsage();
    
    /**
     * Returns the current capacity of the map.
     * 
     * <p>This is the number of entries that can be stored without resizing.
     * 
     * @return current capacity
     */
    long capacity();
    
    /**
     * Returns the load factor.
     * 
     * <p>Load factor = size / capacity
     * 
     * @return load factor (0.0 - 1.0)
     */
    default double loadFactor() {
        long cap = capacity();
        if (cap == 0) {
            return 0.0;
        }
        return (double) size() / cap;
    }
    
    /**
     * Creates a persistent snapshot of this map to disk.
     * 
     * <p>The snapshot includes all current entries and can be loaded later
     * with {@link #load(Path)}. This operation is atomic - either the entire
     * snapshot succeeds or it fails without creating a partial file.
     * 
     * @param path the file path to write to
     * @throws IOException if I/O error occurs
     * @throws NullPointerException if path is null
     */
    void snapshot(Path path) throws IOException;
    
    /**
     * Creates a snapshot asynchronously.
     * 
     * <p>This returns immediately while the snapshot is written in the background.
     * The map remains fully operational during the snapshot.
     * 
     * @param path the file path to write to
     * @return a future that completes when snapshot is done
     * @throws NullPointerException if path is null
     */
    java.util.concurrent.CompletableFuture<Void> snapshotAsync(Path path);
    
    /**
     * Closes this map and frees all native memory.
     * 
     * <p>After closing, all operations will throw {@link IllegalStateException}.
     * This operation is idempotent.
     */
    @Override
    void close();
    
    /**
     * Checks if this map is closed.
     * 
     * @return true if closed
     */
    boolean isClosed();
    
    /**
     * Creates an off-heap map with the specified key/value types and initial capacity.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param keyType the key class
     * @param valueType the value class
     * @param initialCapacity the initial capacity
     * @return a new off-heap map
     * @throws IllegalArgumentException if capacity is negative
     * @throws NullPointerException if keyType or valueType is null
     */
    static <K, V> OffHeapMap<K, V> create(Class<K> keyType, Class<V> valueType, long initialCapacity) {
        return OffHeapMapFactory.create(keyType, valueType, initialCapacity);
    }
    
    /**
     * Creates an off-heap map with default initial capacity (16).
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param keyType the key class
     * @param valueType the value class
     * @return a new off-heap map
     * @throws NullPointerException if keyType or valueType is null
     */
    static <K, V> OffHeapMap<K, V> create(Class<K> keyType, Class<V> valueType) {
        return create(keyType, valueType, 16);
    }
    
    /**
     * Loads an off-heap map from a snapshot file.
     * 
     * <p>The file must have been created with {@link #snapshot(Path)}.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param path the snapshot file path
     * @return a new off-heap map loaded with the snapshot data
     * @throws IOException if I/O error occurs or file format is invalid
     * @throws NullPointerException if path is null
     */
    static <K, V> OffHeapMap<K, V> load(Path path) throws IOException {
        return OffHeapMapFactory.load(path);
    }
}
