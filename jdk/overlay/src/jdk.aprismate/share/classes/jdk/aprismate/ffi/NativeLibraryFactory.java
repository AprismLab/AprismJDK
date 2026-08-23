package jdk.aprismate.ffi;

import java.lang.foreign.MemorySegment;
import java.nio.file.Path;
import java.util.*;

/**
 * Factory for NativeLibrary instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class NativeLibraryFactory {
    
    private static NativeLibrary cLibrary;
    
    private NativeLibraryFactory() {
        // No instantiation
    }
    
    static NativeLibrary load(String name) {
        Objects.requireNonNull(name, "name");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.ffi.NativeLibraryImpl");
            return (NativeLibrary) implClass.getMethod("load", String.class).invoke(null, name);
        } catch (Exception e) {
            throw new UnsatisfiedLinkError("Cannot load library: " + name + " (requires AprismJDK)");
        }
    }
    
    static NativeLibrary load(Path path) {
        Objects.requireNonNull(path, "path");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.ffi.NativeLibraryImpl");
            return (NativeLibrary) implClass.getMethod("load", Path.class).invoke(null, path);
        } catch (Exception e) {
            throw new UnsatisfiedLinkError("Cannot load library: " + path + " (requires AprismJDK)");
        }
    }
    
    static NativeLibrary.Builder builder() {
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.ffi.NativeLibraryImpl");
            return (NativeLibrary.Builder) implClass.getMethod("builder").invoke(null);
        } catch (Exception e) {
            return new StubBuilder();
        }
    }
    
    static synchronized NativeLibrary cLibrary() {
        if (cLibrary == null) {
            try {
                Class<?> implClass = Class.forName("com.aprismate.agent.ffi.NativeLibraryImpl");
                cLibrary = (NativeLibrary) implClass.getMethod("cLibrary").invoke(null);
            } catch (Exception e) {
                cLibrary = new StubLibrary("c");
            }
        }
        return cLibrary;
    }
    
    /**
     * Stub Builder implementation.
     */
    private static class StubBuilder implements NativeLibrary.Builder {
        
        private String name;
        
        @Override
        public NativeLibrary.Builder name(String name) {
            this.name = name;
            return this;
        }
        
        @Override
        public NativeLibrary.Builder searchPath(String path) {
            return this;
        }
        
        @Override
        public NativeLibrary.Builder searchPath(Path path) {
            return this;
        }
        
        @Override
        public NativeLibrary.Builder lazyLoad(boolean lazy) {
            return this;
        }
        
        @Override
        public NativeLibrary.Builder mode(int mode) {
            return this;
        }
        
        @Override
        public NativeLibrary load() {
            throw new UnsatisfiedLinkError(
                "Cannot load library: " + name + " (requires AprismJDK)");
        }
    }
    
    /**
     * Stub NativeLibrary implementation.
     */
    private static class StubLibrary implements NativeLibrary {
        
        private final String name;
        
        StubLibrary(String name) {
            this.name = name;
        }
        
        @Override
        public String name() {
            return name;
        }
        
        @Override
        public Path path() {
            return null;
        }
        
        @Override
        public boolean isLoaded() {
            return false;
        }
        
        @Override
        public Optional<MemorySegment> find(String name) {
            return Optional.empty();
        }
        
        @Override
        public Optional<MemorySegment> find(String name, String version) {
            return Optional.empty();
        }
        
        @Override
        public String[] symbols() {
            return new String[0];
        }
        
        @Override
        public void unload() {
            // No-op
        }
    }
}
