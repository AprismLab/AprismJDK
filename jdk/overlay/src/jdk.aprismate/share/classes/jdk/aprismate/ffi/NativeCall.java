package jdk.aprismate.ffi;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * NativeCall - Fluent API for native function calls.
 * 
 * <p>This provides a more convenient interface than manually creating
 * FunctionDescriptor and MethodHandle objects.
 * 
 * <h2>Usage Example - Simple Call</h2>
 * <pre>{@code
 * // Call strlen(const char* str)
 * NativeLibrary libc = NativeLibrary.cLibrary();
 * 
 * try (Arena arena = Arena.ofConfined()) {
 *     MemorySegment str = arena.allocateUtf8String("Hello");
 *     
 *     long length = NativeCall.to(libc, "strlen")
 *         .returns(long.class)
 *         .args(MemorySegment.class)
 *         .call(str);
 *     
 *     System.out.println("Length: " + length);  // 5
 * }
 * }</pre>
 * 
 * <h2>Usage Example - Complex Call</h2>
 * <pre>{@code
 * // Call open(const char* path, int flags, int mode)
 * int fd = NativeCall.to(libc, "open")
 *     .returns(int.class)
 *     .args(MemorySegment.class, int.class, int.class)
 *     .call(pathSegment, O_RDONLY, 0);
 * }</pre>
 * 
 * <h2>Usage Example - Reusable Handle</h2>
 * <pre>{@code
 * // Create reusable handle
 * NativeCall.Handle<Long> strlen = NativeCall.to(libc, "strlen")
 *     .returns(long.class)
 *     .args(MemorySegment.class)
 *     .prepare();
 * 
 * // Call multiple times
 * long len1 = strlen.call(str1);
 * long len2 = strlen.call(str2);
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface NativeCall {
    
    /**
     * Starts building a native call.
     * 
     * @param library the native library
     * @param symbol the function symbol name
     * @return a builder
     */
    static Builder to(NativeLibrary library, String symbol) {
        return NativeCallFactory.to(library, symbol);
    }
    
    /**
     * Starts building a native call with direct symbol address.
     * 
     * @param address the function address
     * @return a builder
     */
    static Builder to(MemorySegment address) {
        return NativeCallFactory.to(address);
    }
    
    /**
     * Builder for native calls.
     */
    interface Builder {
        
        /**
         * Sets the return type.
         * 
         * @param returnType the return type class
         * @return this builder
         */
        Builder returns(Class<?> returnType);
        
        /**
         * Declares no return value (void).
         * 
         * @return this builder
         */
        Builder returnsVoid();
        
        /**
         * Sets the argument types.
         * 
         * @param argTypes the argument type classes
         * @return this builder
         */
        Builder args(Class<?>... argTypes);
        
        /**
         * Enables varargs support.
         * 
         * <p>The last parameter type will be treated as varargs.
         * 
         * @return this builder
         */
        Builder varargs();
        
        /**
         * Sets the calling convention.
         * 
         * @param convention the linker option for calling convention
         * @return this builder
         */
        Builder convention(Linker.Option convention);
        
        /**
         * Enables critical call optimization.
         * 
         * <p>Critical calls skip thread state transitions for better performance,
         * but cannot call back into Java or block.
         * 
         * @return this builder
         */
        Builder critical();
        
        /**
         * Calls the native function immediately.
         * 
         * @param args the arguments
         * @return the return value
         * @throws Throwable if the call fails
         */
        Object call(Object... args) throws Throwable;
        
        /**
         * Prepares a reusable handle.
         * 
         * @param <T> the return type
         * @return a reusable handle
         */
        <T> Handle<T> prepare();
    }
    
    /**
     * Reusable native call handle.
     * 
     * @param <T> the return type
     */
    interface Handle<T> {
        
        /**
         * Calls the native function.
         * 
         * @param args the arguments
         * @return the return value
         * @throws Throwable if the call fails
         */
        T call(Object... args) throws Throwable;
        
        /**
         * Returns the underlying method handle.
         * 
         * @return the method handle
         */
        MethodHandle methodHandle();
        
        /**
         * Returns the function descriptor.
         * 
         * @return the descriptor
         */
        FunctionDescriptor descriptor();
    }
}
