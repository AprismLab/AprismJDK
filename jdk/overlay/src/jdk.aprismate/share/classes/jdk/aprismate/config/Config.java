package jdk.aprismate.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a configuration with hierarchical key-value pairs.
 * <p>
 * Configurations support nested structures using dot notation for keys
 * (e.g., "server.port", "database.connection.timeout"). Values can be
 * primitives, strings, lists, or nested configurations.
 * </p>
 * <p>
 * Implementations should be thread-safe for reading, though writing may
 * require external synchronization depending on the implementation.
 * </p>
 *
 * @since 26.0-Alpha.6
 */
public interface Config {
    
    /**
     * Gets a string value from the configuration.
     *
     * @param key the configuration key
     * @return the value, or empty if not found or not a string
     * @throws NullPointerException if key is null
     */
    Optional<String> getString(String key);
    
    /**
     * Gets a string value with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the value, or defaultValue if not found
     * @throws NullPointerException if key is null
     */
    default String getString(String key, String defaultValue) {
        return getString(key).orElse(defaultValue);
    }
    
    /**
     * Gets an integer value from the configuration.
     *
     * @param key the configuration key
     * @return the value, or empty if not found or not an integer
     * @throws NullPointerException if key is null
     */
    Optional<Integer> getInt(String key);
    
    /**
     * Gets an integer value with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the value, or defaultValue if not found
     * @throws NullPointerException if key is null
     */
    default int getInt(String key, int defaultValue) {
        return getInt(key).orElse(defaultValue);
    }
    
    /**
     * Gets a long value from the configuration.
     *
     * @param key the configuration key
     * @return the value, or empty if not found or not a long
     * @throws NullPointerException if key is null
     */
    Optional<Long> getLong(String key);
    
    /**
     * Gets a long value with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the value, or defaultValue if not found
     * @throws NullPointerException if key is null
     */
    default long getLong(String key, long defaultValue) {
        return getLong(key).orElse(defaultValue);
    }
    
    /**
     * Gets a double value from the configuration.
     *
     * @param key the configuration key
     * @return the value, or empty if not found or not a double
     * @throws NullPointerException if key is null
     */
    Optional<Double> getDouble(String key);
    
    /**
     * Gets a double value with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the value, or defaultValue if not found
     * @throws NullPointerException if key is null
     */
    default double getDouble(String key, double defaultValue) {
        return getDouble(key).orElse(defaultValue);
    }
    
    /**
     * Gets a boolean value from the configuration.
     *
     * @param key the configuration key
     * @return the value, or empty if not found or not a boolean
     * @throws NullPointerException if key is null
     */
    Optional<Boolean> getBoolean(String key);
    
    /**
     * Gets a boolean value with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the value, or defaultValue if not found
     * @throws NullPointerException if key is null
     */
    default boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key).orElse(defaultValue);
    }
    
    /**
     * Gets a list of strings from the configuration.
     *
     * @param key the configuration key
     * @return the list, or empty if not found or not a string list
     * @throws NullPointerException if key is null
     */
    Optional<List<String>> getStringList(String key);
    
    /**
     * Gets a list of strings with a default.
     *
     * @param key the configuration key
     * @param defaultValue the default value if key is not found
     * @return the list, or defaultValue if not found
     * @throws NullPointerException if key is null
     */
    default List<String> getStringList(String key, List<String> defaultValue) {
        return getStringList(key).orElse(defaultValue);
    }
    
    /**
     * Gets a nested configuration section.
     *
     * @param key the configuration key
     * @return the nested config, or empty if not found or not a section
     * @throws NullPointerException if key is null
     */
    Optional<Config> getSection(String key);
    
    /**
     * Checks if a key exists in the configuration.
     *
     * @param key the configuration key
     * @return true if the key exists, false otherwise
     * @throws NullPointerException if key is null
     */
    boolean contains(String key);
    
    /**
     * Returns all keys in this configuration (non-recursive).
     *
     * @return set of keys at this level, never null
     */
    Set<String> getKeys();
    
    /**
     * Returns all keys in this configuration recursively.
     * <p>
     * Nested keys are returned in dot notation (e.g., "server.port").
     * </p>
     *
     * @return set of all keys including nested ones, never null
     */
    Set<String> getAllKeys();
    
    /**
     * Sets a value in the configuration.
     * <p>
     * This method may not be supported by read-only configurations.
     * </p>
     *
     * @param key the configuration key
     * @param value the value to set (null removes the key)
     * @throws NullPointerException if key is null
     * @throws UnsupportedOperationException if this config is read-only
     */
    void set(String key, Object value);
    
    /**
     * Removes a key from the configuration.
     *
     * @param key the configuration key
     * @throws NullPointerException if key is null
     * @throws UnsupportedOperationException if this config is read-only
     */
    default void remove(String key) {
        set(key, null);
    }
    
    /**
     * Checks if this configuration is empty.
     *
     * @return true if no keys are present, false otherwise
     */
    default boolean isEmpty() {
        return getKeys().isEmpty();
    }
    
    /**
     * Returns the number of keys at the top level.
     *
     * @return the number of keys
     */
    default int size() {
        return getKeys().size();
    }
}
