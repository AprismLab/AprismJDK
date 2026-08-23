package jdk.aprismate.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Manages application and mod configurations.
 * <p>
 * The ConfigManager provides centralized access to configurations, automatic
 * loading/saving, and format detection. It supports multiple configuration
 * formats through pluggable {@link ConfigLoader} implementations.
 * </p>
 *
 * @since 26.0-Alpha.6
 */
public interface ConfigManager {
    
    /**
     * Loads a configuration from a file, auto-detecting the format.
     *
     * @param path the file path to load from
     * @return the loaded configuration
     * @throws IOException if reading fails
     * @throws ConfigException if parsing fails or format is unsupported
     * @throws NullPointerException if path is null
     */
    Config load(Path path) throws IOException, ConfigException;
    
    /**
     * Loads a configuration using a specific loader.
     *
     * @param path the file path to load from
     * @param format the format name (e.g., "json", "yaml")
     * @return the loaded configuration
     * @throws IOException if reading fails
     * @throws ConfigException if parsing fails or format is unsupported
     * @throws NullPointerException if path or format is null
     */
    Config load(Path path, String format) throws IOException, ConfigException;
    
    /**
     * Saves a configuration to a file, auto-detecting the format from extension.
     *
     * @param config the configuration to save
     * @param path the file path to save to
     * @throws IOException if writing fails
     * @throws ConfigException if serialization fails or format is unsupported
     * @throws NullPointerException if config or path is null
     */
    void save(Config config, Path path) throws IOException, ConfigException;
    
    /**
     * Saves a configuration using a specific loader.
     *
     * @param config the configuration to save
     * @param path the file path to save to
     * @param format the format name (e.g., "json", "yaml")
     * @throws IOException if writing fails
     * @throws ConfigException if serialization fails or format is unsupported
     * @throws NullPointerException if config, path, or format is null
     */
    void save(Config config, Path path, String format) throws IOException, ConfigException;
    
    /**
     * Gets or creates a configuration for the given mod.
     * <p>
     * If the configuration file doesn't exist, creates an empty one.
     * The configuration is cached and automatically saved on changes.
     * </p>
     *
     * @param modId the mod identifier
     * @param fileName the configuration file name (e.g., "config.json")
     * @return the mod's configuration
     * @throws IOException if loading or creating fails
     * @throws ConfigException if parsing fails
     * @throws NullPointerException if modId or fileName is null
     */
    Config getModConfig(String modId, String fileName) throws IOException, ConfigException;
    
    /**
     * Gets the system configuration.
     * <p>
     * This is the main AprismJDK configuration loaded from the system
     * config directory.
     * </p>
     *
     * @return the system configuration
     * @throws IOException if loading fails
     * @throws ConfigException if parsing fails
     */
    Config getSystemConfig() throws IOException, ConfigException;
    
    /**
     * Registers a configuration loader for a specific format.
     *
     * @param loader the loader to register
     * @throws NullPointerException if loader is null
     * @throws IllegalArgumentException if a loader for this format already exists
     */
    void registerLoader(ConfigLoader loader);
    
    /**
     * Gets a registered configuration loader by format name.
     *
     * @param format the format name (e.g., "json", "yaml")
     * @return the loader, or empty if not registered
     * @throws NullPointerException if format is null
     */
    Optional<ConfigLoader> getLoader(String format);
    
    /**
     * Gets a configuration loader that can handle the given file.
     *
     * @param path the file path
     * @return the loader, or empty if no loader supports this file
     * @throws NullPointerException if path is null
     */
    Optional<ConfigLoader> getLoaderForFile(Path path);
    
    /**
     * Reloads all cached configurations from disk.
     *
     * @throws IOException if reloading fails
     * @throws ConfigException if parsing fails
     */
    void reloadAll() throws IOException, ConfigException;
    
    /**
     * Saves all modified configurations to disk.
     *
     * @throws IOException if saving fails
     * @throws ConfigException if serialization fails
     */
    void saveAll() throws IOException, ConfigException;
    
    /**
     * Returns the configuration directory for mods.
     *
     * @return the config directory path, never null
     */
    Path getConfigDirectory();
}
