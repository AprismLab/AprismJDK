package jdk.aprismate.mod;

import java.util.List;
import java.util.Optional;

/**
 * Metadata about a mod.
 * <p>
 * This interface provides access to information about a mod, including its
 * identifier, version, dependencies, authors, and other descriptive metadata.
 * </p>
 * <p>
 * Metadata is typically loaded from a {@code mod.json} file in the mod's JAR.
 * </p>
 *
 * @since 26.0-Alpha.4
 */
public interface ModMetadata {
    
    /**
     * Returns the unique identifier of this mod.
     * <p>
     * The mod ID must:
     * <ul>
     *   <li>Be unique across all mods</li>
     *   <li>Contain only lowercase letters, digits, hyphens, and underscores</li>
     *   <li>Start with a letter</li>
     *   <li>Be between 3 and 64 characters long</li>
     * </ul>
     * </p>
     *
     * @return the mod ID, never {@code null}
     */
    String getId();
    
    /**
     * Returns the version of this mod.
     * <p>
     * Versions should follow semantic versioning (e.g., "1.0.0", "2.1.3-beta").
     * </p>
     *
     * @return the mod version, never {@code null}
     */
    String getVersion();
    
    /**
     * Returns the human-readable name of this mod.
     *
     * @return the mod name, never {@code null}
     */
    String getName();
    
    /**
     * Returns a brief description of this mod.
     *
     * @return the description, or empty if not provided
     */
    Optional<String> getDescription();
    
    /**
     * Returns the list of authors who created this mod.
     *
     * @return the author list, never {@code null} but may be empty
     */
    List<String> getAuthors();
    
    /**
     * Returns the list of mods that this mod depends on.
     * <p>
     * Each dependency specifies a mod ID and optional version constraint.
     * If a required dependency is missing or has an incompatible version,
     * the mod will fail to load.
     * </p>
     *
     * @return the dependency list, never {@code null} but may be empty
     */
    List<ModDependency> getDependencies();
    
    /**
     * Returns the homepage URL for this mod.
     *
     * @return the homepage URL, or empty if not provided
     */
    Optional<String> getHomepage();
    
    /**
     * Returns the source code repository URL for this mod.
     *
     * @return the source URL, or empty if not provided
     */
    Optional<String> getSource();
    
    /**
     * Returns the issue tracker URL for this mod.
     *
     * @return the issues URL, or empty if not provided
     */
    Optional<String> getIssues();
    
    /**
     * Returns the license identifier for this mod.
     * <p>
     * Should be an SPDX license identifier (e.g., "MIT", "Apache-2.0", "GPL-3.0").
     * </p>
     *
     * @return the license, or empty if not provided
     */
    Optional<String> getLicense();
    
    /**
     * Returns the path to the mod's icon file within its JAR.
     *
     * @return the icon path, or empty if not provided
     */
    Optional<String> getIcon();
    
    /**
     * Returns the environment(s) where this mod can run.
     *
     * @return the mod environment, never {@code null}
     */
    ModEnvironment getEnvironment();
    
    /**
     * Returns a custom metadata value by key.
     * <p>
     * This allows mods to store arbitrary metadata beyond the standard fields.
     * </p>
     *
     * @param key the metadata key, must not be {@code null}
     * @return the metadata value, or empty if not present
     */
    Optional<String> getCustom(String key);
}
