package jdk.aprismate.resource;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Provides resources from a specific source (mod, resource pack, system).
 * <p>
 * ResourceProviders are registered with the ResourceManager and queried
 * when resources are requested. Multiple providers can supply the same
 * resource, with later providers taking precedence.
 * </p>
 *
 * @since 26.0-Alpha.5
 */
public interface ResourceProvider {
    
    /**
     * Returns the unique identifier for this provider.
     * <p>
     * This is typically the mod ID or "system" for built-in resources.
     * </p>
     *
     * @return the provider ID, never null
     */
    String getId();
    
    /**
     * Returns the display name of this provider.
     *
     * @return the display name, never null
     */
    String getName();
    
    /**
     * Gets a resource from this provider.
     *
     * @param location the resource location
     * @return the resource, or null if not provided by this provider
     */
    Resource getResource(ResourceLocation location);
    
    /**
     * Returns all resources provided by this provider.
     *
     * @return collection of all resource locations, never null
     */
    Collection<ResourceLocation> listResources();
    
    /**
     * Returns all resources in the given namespace.
     *
     * @param namespace the namespace to search
     * @return collection of resource locations in the namespace, never null
     */
    Collection<ResourceLocation> listResourcesInNamespace(String namespace);
    
    /**
     * Returns all namespaces provided by this provider.
     *
     * @return collection of namespace strings, never null
     */
    Collection<String> getNamespaces();
    
    /**
     * Returns the root path of this provider, if applicable.
     * <p>
     * For file-based providers, this returns the directory or JAR path.
     * For dynamic providers, this returns empty.
     * </p>
     *
     * @return the root path, or empty if not applicable
     */
    default java.util.Optional<Path> getRootPath() {
        return java.util.Optional.empty();
    }
    
    /**
     * Returns the priority of this provider.
     * <p>
     * Higher priority providers override lower priority ones when
     * multiple providers supply the same resource.
     * </p>
     * <p>
     * Default priority is 0. System resources typically use 1000,
     * mods use 0-999, and user resource packs use negative values.
     * </p>
     *
     * @return the priority value
     */
    default int getPriority() {
        return 0;
    }
}
