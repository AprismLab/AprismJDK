package jdk.aprismate.jit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * ProfileData - Profile-guided optimization data.
 * 
 * <p>Contains execution statistics collected during profiling runs,
 * including branch frequencies, call sites, type profiles, etc.
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * // Training phase
 * JitCompiler.enableProfiling();
 * runWorkload();
 * ProfileData profiles = JitCompiler.collectProfiles();
 * profiles.save(Path.of("profiles.dat"));
 * 
 * // Production phase
 * ProfileData profiles = ProfileData.load(Path.of("profiles.dat"));
 * JitCompiler.applyProfiles(profiles);
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface ProfileData {
    
    /**
     * Returns the number of profiled methods.
     * 
     * @return method count
     */
    int methodCount();
    
    /**
     * Returns the number of profiled branches.
     * 
     * @return branch count
     */
    long branchCount();
    
    /**
     * Returns the number of profiled call sites.
     * 
     * @return call site count
     */
    long callSiteCount();
    
    /**
     * Returns branch frequencies for all methods.
     * 
     * <p>Maps method name to branch ID to taken frequency (0.0 - 1.0).
     * 
     * @return branch frequency map
     */
    Map<String, Map<Integer, Double>> branchFrequencies();
    
    /**
     * Returns call site targets.
     * 
     * <p>Maps caller method to call site ID to callee method frequencies.
     * 
     * @return call site target map
     */
    Map<String, Map<Integer, Map<String, Long>>> callSiteTargets();
    
    /**
     * Returns type profiles.
     * 
     * <p>Maps method to type check location to observed types and frequencies.
     * 
     * @return type profile map
     */
    Map<String, Map<Integer, Map<Class<?>, Long>>> typeProfiles();
    
    /**
     * Saves this profile data to a file.
     * 
     * @param path the file path
     * @throws IOException if I/O error occurs
     * @throws NullPointerException if path is null
     */
    void save(Path path) throws IOException;
    
    /**
     * Loads profile data from a file.
     * 
     * @param path the file path
     * @return loaded profile data
     * @throws IOException if I/O error occurs or file format is invalid
     * @throws NullPointerException if path is null
     */
    static ProfileData load(Path path) throws IOException {
        return ProfileDataFactory.load(path);
    }
    
    /**
     * Merges multiple profile datasets.
     * 
     * <p>Useful for combining profiles from multiple training runs.
     * 
     * @param profiles the profiles to merge
     * @return merged profile data
     * @throws NullPointerException if profiles is null
     */
    static ProfileData merge(ProfileData... profiles) {
        return ProfileDataFactory.merge(profiles);
    }
}
