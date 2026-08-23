package jdk.aprismate.ffi;

import java.lang.foreign.*;
import java.nio.file.Path;
import java.util.Optional;

/**
 * NativeLibrary - Enhanced native library loading and symbol lookup.
 * 
 * <p>This API extends the standard {@link java.lang.foreign.SymbolLookup}
 * with additional features:
 * <ul>
 *   <li>Automatic library path discovery</li>
 *   <li>Symbol versioning support</li>
 *   <li>Lazy loading and unloading</li>
 *   <li>Cross-platform path resolution</li>
 * </ul>
 * 
 * <h2>Usage Example - Basic</h2>
 * <pre>{@code
 * // Load a library
 * NativeLibrary lib = NativeLibrary.load("mylib");
 * 
 * // Lookup a symbol
 * Optional<MemorySegment> symbol = lib.find("my_function");
 * 
 * // Create a downcall handle
 * FunctionDescriptor desc = FunctionDescriptor.of(
 *     ValueLayout.JAVA_INT, 
 *     ValueLayout.JAVA_INT
 * );
 * MethodHandle handle = Linker.nativeLinker().downcallHandle(
 *     symbol.orElseThrow(), 
 *     desc
 * );
 * 
 * // Call native function
 * int result = (int) handle.invoke(42);
 * }</pre>
 * 
 * <h2>Usage Example - Advanced</h2>
 * <pre>{@code
 * // Load with custom search paths
 * NativeLibrary lib = NativeLibrary.builder()
 *     .name("mylib")
 *     .searchPath("/opt/mylib/lib")
 *     .searchPath("/usr/local/lib")
 *     .lazyLoad(true)
 *     .load();
 * 
 * // Symbol with version
 * Optional<MemorySegment> symbol = lib.find("my_function", "2.0");
 * 
 * // Check if loaded
 * if (lib.isLoaded()) {
 *     // Use library...
 * }
 * 
 * // Unload when done
 * lib.unload();
 * }</pre>
 * 
 * <h2>Cross-Platform Support</h2>
 * <p>Library names are automatically mapped to platform conventions:
 * <ul>
 *   <li>Linux: libmylib.so</li>
 *   <li>macOS: libmylib.dylib</li>
 *   <li>Windows: mylib.dll</li>
 * </ul>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface NativeLibrary extends SymbolLookup, AutoCloseable {
    
    /**
     * Returns the library name.
     * 
     * @return library name
     */
    String name();
    
    /**
     * Returns the library path.
     * 
     * @return library path, or null if not yet loaded
     */
    Path path();
    
    /**
     * Checks if this library is loaded.
     * 
     * @return true if loaded
     */
    boolean isLoaded();
    
    /**
     * Finds a symbol with versioning support.
     * 
     * <p>This is useful for Linux shared libraries that use symbol versioning.
     * 
     * @param name the symbol name
     * @param version the symbol version (e.g., "GLIBC_2.17")
     * @return the symbol address, or empty if not found
     * @throws IllegalStateException if library is not loaded
     */
    Optional<MemorySegment> find(String name, String version);
    
    /**
     * Lists all exported symbols.
     * 
     * <p>This parses the library's symbol table.
     * 
     * @return array of symbol names
     * @throws IllegalStateException if library is not loaded
     */
    String[] symbols();
    
    /**
     * Checks if a symbol exists.
     * 
     * @param name the symbol name
     * @return true if symbol exists
     */
    default boolean hasSymbol(String name) {
        return find(name).isPresent();
    }
    
    /**
     * Unloads this library.
     * 
     * <p>After unloading, all symbols become invalid and must not be used.
     * This is typically called automatically when the library is no longer
     * referenced.
     */
    void unload();
    
    /**
     * Closes this library (alias for unload).
     */
    @Override
    default void close() {
        unload();
    }
    
    /**
     * Loads a native library by name.
     * 
     * <p>Searches standard library paths and applies platform naming conventions.
     * 
     * @param name the library name (without prefix/suffix)
     * @return a loaded library
     * @throws UnsatisfiedLinkError if library cannot be found or loaded
     */
    static NativeLibrary load(String name) {
        return NativeLibraryFactory.load(name);
    }
    
    /**
     * Loads a native library from a specific path.
     * 
     * @param path the library file path
     * @return a loaded library
     * @throws UnsatisfiedLinkError if library cannot be loaded
     */
    static NativeLibrary load(Path path) {
        return NativeLibraryFactory.load(path);
    }
    
    /**
     * Creates a new library builder.
     * 
     * @return a new builder instance
     */
    static Builder builder() {
        return NativeLibraryFactory.builder();
    }
    
    /**
     * Returns the default C library (libc).
     * 
     * <p>This provides access to standard C functions like malloc, free, etc.
     * 
     * @return the C library
     */
    static NativeLibrary cLibrary() {
        return NativeLibraryFactory.cLibrary();
    }
    
    /**
     * Builder for NativeLibrary.
     */
    interface Builder {
        
        /**
         * Sets the library name.
         * 
         * @param name the library name
         * @return this builder
         */
        Builder name(String name);
        
        /**
         * Adds a search path.
         * 
         * <p>Multiple paths can be added and will be searched in order.
         * 
         * @param path the directory path
         * @return this builder
         */
        Builder searchPath(String path);
        
        /**
         * Adds a search path.
         * 
         * @param path the directory path
         * @return this builder
         */
        Builder searchPath(Path path);
        
        /**
         * Enables or disables lazy loading.
         * 
         * <p>When lazy loading is enabled, the library is not actually loaded
         * until the first symbol is requested. Default is false.
         * 
         * @param lazy true to enable lazy loading
         * @return this builder
         */
        Builder lazyLoad(boolean lazy);
        
        /**
         * Sets the loading mode flags.
         * 
         * <p>On Linux, this corresponds to dlopen flags like RTLD_NOW, RTLD_LAZY, etc.
         * 
         * @param mode the mode flags
         * @return this builder
         */
        Builder mode(int mode);
        
        /**
         * Loads the library with the configured settings.
         * 
         * @return a loaded library
         * @throws UnsatisfiedLinkError if library cannot be loaded
         */
        NativeLibrary load();
    }
}
