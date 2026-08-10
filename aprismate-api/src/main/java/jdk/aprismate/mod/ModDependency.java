package jdk.aprismate.mod;

/**
 * Represents a dependency on another mod.
 * <p>
 * Dependencies specify that a mod requires another mod to be present,
 * and optionally that it must match a certain version constraint.
 * </p>
 *
 * @since 26.0-Alpha.4
 */
public interface ModDependency {
    
    /**
     * Returns the mod ID of the dependency.
     *
     * @return the dependency mod ID, never {@code null}
     */
    String getModId();
    
    /**
     * Returns the version constraint for this dependency.
     * <p>
     * Version constraints use Maven-style version ranges:
     * <ul>
     *   <li>{@code "1.0"} - Any version >= 1.0</li>
     *   <li>{@code "[1.0]"} - Exactly version 1.0</li>
     *   <li>{@code "[1.0,2.0)"} - Version >= 1.0 and < 2.0</li>
     *   <li>{@code "[1.0,)"} - Version >= 1.0</li>
     *   <li>{@code "(,2.0)"} - Version < 2.0</li>
     * </ul>
     * </p>
     * <p>
     * If no constraint is specified, any version is acceptable.
     * </p>
     *
     * @return the version constraint, or {@code "*"} for any version
     */
    String getVersionRange();
    
    /**
     * Returns whether this dependency is required.
     * <p>
     * If {@code true}, the mod will fail to load if this dependency is missing
     * or does not satisfy the version constraint.
     * </p>
     * <p>
     * If {@code false}, the dependency is optional and the mod will load
     * regardless of whether the dependency is present.
     * </p>
     *
     * @return {@code true} if required, {@code false} if optional
     */
    boolean isRequired();
    
    /**
     * Returns the type of this dependency.
     *
     * @return the dependency type, never {@code null}
     */
    DependencyType getType();
    
    /**
     * Enum representing the type of dependency relationship.
     */
    enum DependencyType {
        /**
         * This mod requires the dependency to function.
         * The dependency must be loaded before this mod.
         */
        REQUIRED,
        
        /**
         * This mod can optionally use the dependency if present.
         * If present, the dependency is loaded before this mod.
         */
        OPTIONAL,
        
        /**
         * This mod conflicts with the dependency.
         * Both mods cannot be loaded at the same time.
         */
        CONFLICTS,
        
        /**
         * This mod must load before the dependency.
         * Used to specify load order without a hard dependency.
         */
        BEFORE,
        
        /**
         * This mod must load after the dependency.
         * Used to specify load order without a hard dependency.
         */
        AFTER
    }
}
