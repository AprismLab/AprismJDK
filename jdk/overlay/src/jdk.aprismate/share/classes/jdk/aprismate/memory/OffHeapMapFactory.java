package jdk.aprismate.memory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Factory for creating OffHeapMap instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class OffHeapMapFactory {
    
    private OffHeapMapFactory() {
        // No instantiation
    }
    
    /**
     * Creates an off-heap map.
     */
    @SuppressWarnings("unchecked")
    static <K, V> OffHeapMap<K, V> create(Class<K> keyType, Class<V> valueType, long initialCapacity) {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive: " + initialCapacity);
        }
        
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.memory.OffHeapMapImpl");
            return (OffHeapMap<K, V>) implClass
                .getConstructor(Class.class, Class.class, long.class)
                .newInstance(keyType, valueType, initialCapacity);
        } catch (Exception e) {
            // Fall back to stub implementation
            return new StubOffHeapMap<>(keyType, valueType);
        }
    }
    
    /**
     * Loads an off-heap map from a snapshot.
     */
    @SuppressWarnings("unchecked")
    static <K, V> OffHeapMap<K, V> load(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        
        try {
            // Try to use agent implementation
            Class<?> implClass = Class.forName("com.aprismate.agent.memory.OffHeapMapImpl");
            return (OffHeapMap<K, V>) implClass
                .getMethod("load", Path.class)
                .invoke(null, path);
        } catch (Exception e) {
            throw new IOException("Cannot load off-heap map: requires AprismJDK", e);
        }
    }
    
    /**
     * Stub implementation backed by HashMap.
     */
    private static class StubOffHeapMap<K, V> implements OffHeapMap<K, V> {
        
        private final Class<K> keyType;
        private final Class<V> valueType;
        private final Map<K, V> backing = new HashMap<>();
        private volatile boolean closed = false;
        
        StubOffHeapMap(Class<K> keyType, Class<V> valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
        }
        
        @Override
        public long getLong(K key, long defaultValue) {
            checkClosed();
            V value = backing.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return defaultValue;
        }
        
        @Override
        public int getInt(K key, int defaultValue) {
            checkClosed();
            V value = backing.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return defaultValue;
        }
        
        @Override
        public double getDouble(K key, double defaultValue) {
            checkClosed();
            V value = backing.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return defaultValue;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void putLong(K key, long value) {
            checkClosed();
            backing.put(key, (V) Long.valueOf(value));
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void putInt(K key, int value) {
            checkClosed();
            backing.put(key, (V) Integer.valueOf(value));
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void putDouble(K key, double value) {
            checkClosed();
            backing.put(key, (V) Double.valueOf(value));
        }
        
        @Override
        public Map<K, V> getAll(Collection<? extends K> keys) {
            checkClosed();
            Map<K, V> result = new HashMap<>();
            for (K key : keys) {
                V value = backing.get(key);
                if (value != null) {
                    result.put(key, value);
                }
            }
            return result;
        }
        
        @Override
        public long memoryUsage() {
            // Rough estimate: 64 bytes per entry
            return backing.size() * 64L;
        }
        
        @Override
        public long capacity() {
            return backing.size();
        }
        
        @Override
        public void snapshot(Path path) throws IOException {
            checkClosed();
            throw new UnsupportedOperationException(
                "Snapshot requires AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public CompletableFuture<Void> snapshotAsync(Path path) {
            checkClosed();
            return CompletableFuture.failedFuture(
                new UnsupportedOperationException(
                    "Snapshot requires AprismJDK. Running on stock JDK."));
        }
        
        @Override
        public void close() {
            closed = true;
            backing.clear();
        }
        
        @Override
        public boolean isClosed() {
            return closed;
        }
        
        private void checkClosed() {
            if (closed) {
                throw new IllegalStateException("Map is closed");
            }
        }
        
        // Delegate to backing map
        
        @Override
        public int size() {
            return backing.size();
        }
        
        @Override
        public boolean isEmpty() {
            return backing.isEmpty();
        }
        
        @Override
        public boolean containsKey(Object key) {
            return backing.containsKey(key);
        }
        
        @Override
        public boolean containsValue(Object value) {
            return backing.containsValue(value);
        }
        
        @Override
        public V get(Object key) {
            checkClosed();
            return backing.get(key);
        }
        
        @Override
        public V put(K key, V value) {
            checkClosed();
            return backing.put(key, value);
        }
        
        @Override
        public V remove(Object key) {
            checkClosed();
            return backing.remove(key);
        }
        
        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            checkClosed();
            backing.putAll(m);
        }
        
        @Override
        public void clear() {
            checkClosed();
            backing.clear();
        }
        
        @Override
        public Set<K> keySet() {
            checkClosed();
            return backing.keySet();
        }
        
        @Override
        public Collection<V> values() {
            checkClosed();
            return backing.values();
        }
        
        @Override
        public Set<Entry<K, V>> entrySet() {
            checkClosed();
            return backing.entrySet();
        }
    }
}
