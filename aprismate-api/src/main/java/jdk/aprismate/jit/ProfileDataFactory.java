package jdk.aprismate.jit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Factory for ProfileData instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class ProfileDataFactory {
    
    private ProfileDataFactory() {
        // No instantiation
    }
    
    /**
     * Loads profile data from a file.
     */
    static ProfileData load(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.ProfileDataImpl");
            return (ProfileData) implClass.getMethod("load", Path.class).invoke(null, path);
        } catch (Exception e) {
            throw new IOException("Cannot load profile data: requires AprismJDK", e);
        }
    }
    
    /**
     * Merges multiple profile datasets.
     */
    static ProfileData merge(ProfileData... profiles) {
        Objects.requireNonNull(profiles, "profiles");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.jit.ProfileDataImpl");
            return (ProfileData) implClass.getMethod("merge", ProfileData[].class)
                .invoke(null, (Object) profiles);
        } catch (Exception e) {
            return empty();
        }
    }
    
    /**
     * Creates an empty profile data.
     */
    static ProfileData empty() {
        return new StubProfileData();
    }
    
    /**
     * Stub ProfileData implementation.
     */
    private static class StubProfileData implements ProfileData {
        
        @Override
        public int methodCount() {
            return 0;
        }
        
        @Override
        public long branchCount() {
            return 0;
        }
        
        @Override
        public long callSiteCount() {
            return 0;
        }
        
        @Override
        public Map<String, Map<Integer, Double>> branchFrequencies() {
            return Collections.emptyMap();
        }
        
        @Override
        public Map<String, Map<Integer, Map<String, Long>>> callSiteTargets() {
            return Collections.emptyMap();
        }
        
        @Override
        public Map<String, Map<Integer, Map<Class<?>, Long>>> typeProfiles() {
            return Collections.emptyMap();
        }
        
        @Override
        public void save(Path path) throws IOException {
            throw new UnsupportedOperationException(
                "Profile data save requires AprismJDK. Running on stock JDK.");
        }
    }
}
