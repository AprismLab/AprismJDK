package jdk.aprismate.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Manages loading and accessing resources from various sources.
 * <p>
 * ResourceManager provides a unified interface for loading resources from
 * the file system, JAR files, network, or other sources. Resources are
 * identified by {@link ResourceLocation} and can be filtered by namespace,
 * path patterns, or custom predicates.
 * </p>
 *
 * @since 26.0-Alpha.5
 */
public interface ResourceManager {
    
    /**
     * Gets a resource by its location.
     *
     * @param location the resource location
     * @return the resource, or empty if not found
     * @throws NullPointerException if location is null
     */
    Optional<Resource> getResource(ResourceLocation location);
    
    /**
     * Gets all resources matching the given location.
     * <p>
     * Multiple resources can exist for the same location if they come from
     * different sources (e.g., multiple mods providing the same resource).
     * </p>
     *
     * @param location the resource location
     * @return collection of all matching resources, never null
     * @throws NullPointerException if location is null
     */
    Collection<Resource> getAllResources(ResourceLocation location);
    
    /**
     * Finds all resources matching the given predicate.
     *
     * @param filter the predicate to test resource locations
     * @return collection of matching resources, never null
     * @throws NullPointerException if filter is null
     */
    Collection<Resource> findResources(Predicate<ResourceLocation> filter);
    
    /**
     * Finds all resources in the given namespace.
     *
     * @param namespace the namespace to search
     * @return collection of resources in the namespace, never null
     * @throws NullPointerException if namespace is null
     */
    Collection<Resource> findResourcesInNamespace(String namespace);
    
    /**
     * Finds all resources with paths starting with the given prefix.
     *
     * @param pathPrefix the path prefix to search for
     * @return collection of matching resources, never null
     * @throws NullPointerException if pathPrefix is null
     */
    Collection<Resource> findResourcesByPath(String pathPrefix);
    
    /**
     * Checks if a resource exists at the given location.
     *
     * @param location the resource location
     * @return true if at least one resource exists at this location
     * @throws NullPointerException if location is null
     */
    default boolean hasResource(ResourceLocation location) {
        return getResource(location).isPresent();
    }
    
    /**
     * Opens an input stream to read the resource.
     * <p>
     * If multiple resources exist for this location, returns the highest
     * priority one (typically the last loaded mod).
     * </p>
     * <p>
     * The caller is responsible for closing the stream.
     * </p>
     *
     * @param location the resource location
     * @return an input stream, or empty if resource not found
     * @throws NullPointerException if location is null
     */
    default Optional<InputStream> open(ResourceLocation location) {
        return getResource(location).flatMap(resource -> {
            try {
                return Optional.of(resource.open());
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }
    
    /**
     * Reloads all resources from their sources.
     * <p>
     * This is typically called when mods are loaded or when the user
     * changes resource packs.
     * </p>
     *
     * @throws IOException if reloading fails
     */
    void reload() throws IOException;
    
    /**
     * Returns all available namespaces.
     *
     * @return collection of namespace strings, never null
     */
    Collection<String> getNamespaces();
}
