package jdk.aprismate.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Tests for Config interface.
 */
class ConfigTest {
    
    @Test
    void getStringShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("name", "test");
        
        Optional<String> value = config.getString("name");
        assertTrue(value.isPresent());
        assertEquals("test", value.get());
    }
    
    @Test
    void getStringShouldReturnEmptyWhenNotFound() {
        TestConfig config = new TestConfig();
        
        Optional<String> value = config.getString("missing");
        assertFalse(value.isPresent());
    }
    
    @Test
    void getStringWithDefaultShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("name", "test");
        
        String value = config.getString("name", "default");
        assertEquals("test", value);
    }
    
    @Test
    void getStringWithDefaultShouldReturnDefaultWhenNotFound() {
        TestConfig config = new TestConfig();
        
        String value = config.getString("missing", "default");
        assertEquals("default", value);
    }
    
    @Test
    void getIntShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("port", 8080);
        
        Optional<Integer> value = config.getInt("port");
        assertTrue(value.isPresent());
        assertEquals(8080, value.get());
    }
    
    @Test
    void getIntWithDefaultShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("port", 8080);
        
        int value = config.getInt("port", 9090);
        assertEquals(8080, value);
    }
    
    @Test
    void getIntWithDefaultShouldReturnDefaultWhenNotFound() {
        TestConfig config = new TestConfig();
        
        int value = config.getInt("missing", 9090);
        assertEquals(9090, value);
    }
    
    @Test
    void getLongShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("timeout", 1000L);
        
        Optional<Long> value = config.getLong("timeout");
        assertTrue(value.isPresent());
        assertEquals(1000L, value.get());
    }
    
    @Test
    void getLongWithDefaultShouldReturnDefaultWhenNotFound() {
        TestConfig config = new TestConfig();
        
        long value = config.getLong("missing", 2000L);
        assertEquals(2000L, value);
    }
    
    @Test
    void getDoubleShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("scale", 1.5);
        
        Optional<Double> value = config.getDouble("scale");
        assertTrue(value.isPresent());
        assertEquals(1.5, value.get(), 0.001);
    }
    
    @Test
    void getDoubleWithDefaultShouldReturnDefaultWhenNotFound() {
        TestConfig config = new TestConfig();
        
        double value = config.getDouble("missing", 2.5);
        assertEquals(2.5, value, 0.001);
    }
    
    @Test
    void getBooleanShouldReturnValue() {
        TestConfig config = new TestConfig();
        config.data.put("enabled", true);
        
        Optional<Boolean> value = config.getBoolean("enabled");
        assertTrue(value.isPresent());
        assertTrue(value.get());
    }
    
    @Test
    void getBooleanWithDefaultShouldReturnDefaultWhenNotFound() {
        TestConfig config = new TestConfig();
        
        boolean value = config.getBoolean("missing", false);
        assertFalse(value);
    }
    
    @Test
    void getStringListShouldReturnValue() {
        TestConfig config = new TestConfig();
        List<String> list = Arrays.asList("a", "b", "c");
        config.data.put("items", list);
        
        Optional<List<String>> value = config.getStringList("items");
        assertTrue(value.isPresent());
        assertEquals(list, value.get());
    }
    
    @Test
    void getStringListWithDefaultShouldReturnDefaultWhenNotFound() {
        TestConfig config = new TestConfig();
        List<String> defaultList = Arrays.asList("x", "y");
        
        List<String> value = config.getStringList("missing", defaultList);
        assertEquals(defaultList, value);
    }
    
    @Test
    void getSectionShouldReturnNestedConfig() {
        TestConfig config = new TestConfig();
        TestConfig nested = new TestConfig();
        nested.data.put("value", "nested");
        config.data.put("section", nested);
        
        Optional<Config> value = config.getSection("section");
        assertTrue(value.isPresent());
        assertEquals("nested", value.get().getString("value").orElse(null));
    }
    
    @Test
    void containsShouldReturnTrueWhenKeyExists() {
        TestConfig config = new TestConfig();
        config.data.put("key", "value");
        
        assertTrue(config.contains("key"));
    }
    
    @Test
    void containsShouldReturnFalseWhenKeyNotExists() {
        TestConfig config = new TestConfig();
        
        assertFalse(config.contains("missing"));
    }
    
    @Test
    void getKeysShouldReturnAllKeys() {
        TestConfig config = new TestConfig();
        config.data.put("key1", "value1");
        config.data.put("key2", "value2");
        
        Set<String> keys = config.getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }
    
    @Test
    void setShouldStoreValue() {
        TestConfig config = new TestConfig();
        config.set("key", "value");
        
        assertEquals("value", config.getString("key").orElse(null));
    }
    
    @Test
    void setShouldRemoveKeyWhenValueIsNull() {
        TestConfig config = new TestConfig();
        config.set("key", "value");
        config.set("key", null);
        
        assertFalse(config.contains("key"));
    }
    
    @Test
    void removeShouldRemoveKey() {
        TestConfig config = new TestConfig();
        config.set("key", "value");
        config.remove("key");
        
        assertFalse(config.contains("key"));
    }
    
    @Test
    void isEmptyShouldReturnTrueWhenEmpty() {
        TestConfig config = new TestConfig();
        assertTrue(config.isEmpty());
    }
    
    @Test
    void isEmptyShouldReturnFalseWhenNotEmpty() {
        TestConfig config = new TestConfig();
        config.set("key", "value");
        assertFalse(config.isEmpty());
    }
    
    @Test
    void sizeShouldReturnNumberOfKeys() {
        TestConfig config = new TestConfig();
        assertEquals(0, config.size());
        
        config.set("key1", "value1");
        assertEquals(1, config.size());
        
        config.set("key2", "value2");
        assertEquals(2, config.size());
    }
    
    // Test implementation of Config
    private static class TestConfig implements Config {
        final Map<String, Object> data = new HashMap<>();
        
        @Override
        public Optional<String> getString(String key) {
            Object value = data.get(key);
            return value instanceof String ? Optional.of((String) value) : Optional.empty();
        }
        
        @Override
        public Optional<Integer> getInt(String key) {
            Object value = data.get(key);
            return value instanceof Integer ? Optional.of((Integer) value) : Optional.empty();
        }
        
        @Override
        public Optional<Long> getLong(String key) {
            Object value = data.get(key);
            return value instanceof Long ? Optional.of((Long) value) : Optional.empty();
        }
        
        @Override
        public Optional<Double> getDouble(String key) {
            Object value = data.get(key);
            return value instanceof Double ? Optional.of((Double) value) : Optional.empty();
        }
        
        @Override
        public Optional<Boolean> getBoolean(String key) {
            Object value = data.get(key);
            return value instanceof Boolean ? Optional.of((Boolean) value) : Optional.empty();
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public Optional<List<String>> getStringList(String key) {
            Object value = data.get(key);
            return value instanceof List ? Optional.of((List<String>) value) : Optional.empty();
        }
        
        @Override
        public Optional<Config> getSection(String key) {
            Object value = data.get(key);
            return value instanceof Config ? Optional.of((Config) value) : Optional.empty();
        }
        
        @Override
        public boolean contains(String key) {
            return data.containsKey(key);
        }
        
        @Override
        public Set<String> getKeys() {
            return new HashSet<>(data.keySet());
        }
        
        @Override
        public Set<String> getAllKeys() {
            return getKeys(); // Simplified for testing
        }
        
        @Override
        public void set(String key, Object value) {
            if (value == null) {
                data.remove(key);
            } else {
                data.put(key, value);
            }
        }
    }
}
