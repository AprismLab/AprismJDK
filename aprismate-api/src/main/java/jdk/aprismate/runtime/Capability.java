package jdk.aprismate.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Describes the capabilities available in this AprismJDK runtime.
 * Capabilities indicate which advanced features are supported by this build.
 * 
 * <p>Capabilities are organized into categories:
 * <ul>
 *   <li><b>Agent</b>: Agent framework features (logging, instrumentation)</li>
 *   <li><b>Transform</b>: Bytecode transformation features (retransform, redefine)</li>
 *   <li><b>Hook</b>: Method hook features (entry/exit hooks)</li>
 *   <li><b>Introspect</b>: VM introspection features (thread, heap, JIT insight)</li>
 *   <li><b>Resource</b>: Resource management features</li>
 * </ul>
 * 
 * @since 26.1-Alpha.1
 */
public final class Capability {
    // Agent capabilities
    public static final String AGENT_LOGGING = "agent.logging";
    public static final String AGENT_INSTRUMENTATION = "agent.instrumentation";
    public static final String AGENT_FAILSAFE = "agent.failsafe";
    
    // Transform capabilities (to be added in later alphas)
    public static final String TRANSFORM_RETRANSFORM = "transform.retransform";
    public static final String TRANSFORM_REDEFINE = "transform.redefine";
    public static final String TRANSFORM_STRUCTURAL = "transform.structural";
    
    // Hook capabilities (to be added in later alphas)
    public static final String HOOK_METHOD_ENTRY = "hook.method.entry";
    public static final String HOOK_METHOD_EXIT = "hook.method.exit";
    public static final String HOOK_JIT_SURVIVAL = "hook.jit.survival";
    
    // Introspection capabilities (to be added in later alphas)
    public static final String INTROSPECT_THREADS = "introspect.threads";
    public static final String INTROSPECT_HEAP = "introspect.heap";
    public static final String INTROSPECT_JIT = "introspect.jit";
    
    // Resource capabilities
    public static final String RESOURCE_MANAGER = "resource.manager";
    public static final String RESOURCE_LEAK_DETECTION = "resource.leak.detection";
    
    private final Map<String, Boolean> capabilities;
    private final String version;
    
    private Capability(String version, Map<String, Boolean> capabilities) {
        this.version = version;
        this.capabilities = Collections.unmodifiableMap(new HashMap<>(capabilities));
    }
    
    /**
     * Returns the AprismJDK version.
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Checks if a specific capability is enabled.
     * 
     * @param capability the capability name (e.g., "agent.logging")
     * @return true if the capability is enabled
     */
    public boolean isEnabled(String capability) {
        return capabilities.getOrDefault(capability, false);
    }
    
    /**
     * Returns all enabled capabilities.
     */
    public Set<String> getEnabledCapabilities() {
        return capabilities.entrySet().stream()
            .filter(Map.Entry::getValue)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
    
    /**
     * Returns all capability names (enabled or disabled).
     */
    public Set<String> getAllCapabilities() {
        return capabilities.keySet();
    }
    
    /**
     * Creates a new capability descriptor builder.
     */
    public static Builder builder(String version) {
        return new Builder(version);
    }
    
    /**
     * Builder for creating Capability descriptors.
     */
    public static final class Builder {
        private final String version;
        private final Map<String, Boolean> capabilities = new HashMap<>();
        
        private Builder(String version) {
            this.version = version;
        }
        
        /**
         * Enables a capability.
         */
        public Builder enable(String capability) {
            capabilities.put(capability, true);
            return this;
        }
        
        /**
         * Disables a capability.
         */
        public Builder disable(String capability) {
            capabilities.put(capability, false);
            return this;
        }
        
        /**
         * Sets a capability to the specified state.
         */
        public Builder set(String capability, boolean enabled) {
            capabilities.put(capability, enabled);
            return this;
        }
        
        /**
         * Builds the Capability descriptor.
         */
        public Capability build() {
            return new Capability(version, capabilities);
        }
    }
    
    @Override
    public String toString() {
        return "Capability{version='" + version + "', enabled=" + getEnabledCapabilities() + "}";
    }
}
