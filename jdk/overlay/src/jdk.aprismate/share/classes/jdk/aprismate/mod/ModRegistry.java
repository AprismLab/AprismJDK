package jdk.aprismate.mod;

import java.util.Collection;
import java.util.Optional;

/**
 * Central registry for accessing loaded mods.
 * <p>
 * This class provides a static API for querying information about loaded mods.
 * It is the primary interface for mods to discover and interact with other mods.
 * </p>
 *
 * @since 26.0-Alpha.4
 */
public final class ModRegistry {
    
    private ModRegistry() {
        throw new UnsupportedOperationException("Cannot instantiate ModRegistry");
    }
    
    /**
     * Returns all loaded mods.
     *
     * @return an unmodifiable collection of all mod containers, never {@code null}
     */
    public static Collection<ModContainer> getAllMods() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Returns the mod container for the specified mod ID.
     *
     * @param modId the mod ID to look up, must not be {@code null}
     * @return the mod container, or empty if not found
     * @throws NullPointerException if modId is {@code null}
     */
    public static Optional<ModContainer> getMod(String modId) {
        if (modId == null) {
            throw new NullPointerException("modId cannot be null");
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Checks if a mod with the given ID is loaded.
     *
     * @param modId the mod ID to check, must not be {@code null}
     * @return {@code true} if the mod is loaded, {@code false} otherwise
     * @throws NullPointerException if modId is {@code null}
     */
    public static boolean isModLoaded(String modId) {
        return getMod(modId).map(ModContainer::isLoaded).orElse(false);
    }
    
    /**
     * Returns the total number of loaded mods.
     *
     * @return the mod count
     */
    public static int getModCount() {
        return getAllMods().size();
    }
    
    /**
     * Returns the mod container that contains the given class.
     * <p>
     * This is useful for mods to determine which mod a class belongs to.
     * </p>
     *
     * @param clazz the class to look up, must not be {@code null}
     * @return the mod container that loaded the class, or empty if not found
     * @throws NullPointerException if clazz is {@code null}
     */
    public static Optional<ModContainer> getModFromClass(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz cannot be null");
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
