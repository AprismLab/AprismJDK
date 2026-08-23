package jdk.aprismate.mod;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents a loaded mod and provides access to its metadata and state.
 * <p>
 * A ModContainer wraps a mod instance along with its metadata, providing
 * a unified interface for the mod loader to manage the mod lifecycle.
 * </p>
 *
 * @since 26.0-Alpha.4
 */
public interface ModContainer {
    
    /**
     * Returns the metadata for this mod.
     *
     * @return the mod metadata, never {@code null}
     */
    ModMetadata getMetadata();
    
    /**
     * Returns the mod instance, if it has been instantiated.
     * <p>
     * The mod instance is created during the mod loading phase.
     * Before that, this method returns empty.
     * </p>
     *
     * @return the mod instance, or empty if not yet instantiated
     */
    Optional<Object> getModInstance();
    
    /**
     * Returns the current state of this mod.
     *
     * @return the mod state, never {@code null}
     */
    ModState getState();
    
    /**
     * Returns the path to the mod's source (typically a JAR file).
     *
     * @return the mod source path, never {@code null}
     */
    Path getSource();
    
    /**
     * Returns the class loader used to load this mod's classes.
     *
     * @return the mod class loader, never {@code null}
     */
    ClassLoader getClassLoader();
    
    /**
     * Returns whether this mod is currently loaded and active.
     *
     * @return {@code true} if the mod is in the {@link ModState#LOADED} state
     */
    default boolean isLoaded() {
        return getState() == ModState.LOADED;
    }
    
    /**
     * Enum representing the lifecycle state of a mod.
     */
    enum ModState {
        /**
         * The mod has been discovered but not yet loaded.
         */
        DISCOVERED,
        
        /**
         * The mod is currently being loaded.
         */
        LOADING,
        
        /**
         * The mod has been successfully loaded and is active.
         */
        LOADED,
        
        /**
         * The mod failed to load due to an error.
         */
        ERRORED,
        
        /**
         * The mod has been disabled by the user or system.
         */
        DISABLED
    }
}
