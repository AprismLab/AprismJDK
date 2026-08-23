package jdk.aprismate.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Handles loading and saving configurations in a specific format.
 * <p>
 * ConfigLoaders support various formats such as JSON, YAML, TOML, Properties,
 * and XML. Each loader is responsible for parsing and serializing configurations
 * in its format.
 * </p>
 *
 * @since 26.0-Alpha.6
 */
public interface ConfigLoader {
    
    /**
     * Returns the format name supported by this loader.
     * <p>
     * Examples: "json", "yaml", "toml", "properties", "xml"
     * </p>
     *
     * @return the format name in lowercase, never null
     */
    String getFormat();
    
    /**
     * Returns the typical file extensions for this format.
     * <p>
     * Examples: [".json"], [".yml", ".yaml"], [".toml"], [".properties"]
     * </p>
     *
     * @return array of file extensions including the dot, never null or empty
     */
    String[] getExtensions();
    
    /**
     * Loads a configuration from an input stream.
     *
     * @param input the input stream to read from
     * @return the loaded configuration, never null
     * @throws IOException if reading fails
     * @throws ConfigException if parsing fails
     * @throws NullPointerException if input is null
     */
    Config load(InputStream input) throws IOException, ConfigException;
    
    /**
     * Loads a configuration from a file.
     *
     * @param path the file path to read from
     * @return the loaded configuration, never null
     * @throws IOException if reading fails
     * @throws ConfigException if parsing fails
     * @throws NullPointerException if path is null
     */
    Config load(Path path) throws IOException, ConfigException;
    
    /**
     * Saves a configuration to an output stream.
     *
     * @param config the configuration to save
     * @param output the output stream to write to
     * @throws IOException if writing fails
     * @throws ConfigException if serialization fails
     * @throws NullPointerException if config or output is null
     */
    void save(Config config, OutputStream output) throws IOException, ConfigException;
    
    /**
     * Saves a configuration to a file.
     *
     * @param config the configuration to save
     * @param path the file path to write to
     * @throws IOException if writing fails
     * @throws ConfigException if serialization fails
     * @throws NullPointerException if config or path is null
     */
    void save(Config config, Path path) throws IOException, ConfigException;
    
    /**
     * Checks if this loader can handle the given file.
     *
     * @param path the file path to check
     * @return true if this loader supports the file extension
     * @throws NullPointerException if path is null
     */
    default boolean canHandle(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : getExtensions()) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates an empty configuration compatible with this loader.
     *
     * @return a new empty configuration, never null
     */
    Config createEmpty();
}
