package jdk.aprismate.ffi;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.*;

/**
 * Factory for NativeCall instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class NativeCallFactory {
    
    private NativeCallFactory() {
        // No instantiation
    }
    
    static NativeCall.Builder to(NativeLibrary library, String symbol) {
        Objects.requireNonNull(library, "library");
        Objects.requireNonNull(symbol, "symbol");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.ffi.NativeCallImpl");
            return (NativeCall.Builder) implClass.getMethod("to", NativeLibrary.class, String.class)
                .invoke(null, library, symbol);
        } catch (Exception e) {
            return new StubBuilder();
        }
    }
    
    static NativeCall.Builder to(MemorySegment address) {
        Objects.requireNonNull(address, "address");
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.ffi.NativeCallImpl");
            return (NativeCall.Builder) implClass.getMethod("to", MemorySegment.class)
                .invoke(null, address);
        } catch (Exception e) {
            return new StubBuilder();
        }
    }
    
    /**
     * Stub Builder implementation.
     */
    private static class StubBuilder implements NativeCall.Builder {
        
        @Override
        public NativeCall.Builder returns(Class<?> returnType) {
            return this;
        }
        
        @Override
        public NativeCall.Builder returnsVoid() {
            return this;
        }
        
        @Override
        public NativeCall.Builder args(Class<?>... argTypes) {
            return this;
        }
        
        @Override
        public NativeCall.Builder varargs() {
            return this;
        }
        
        @Override
        public NativeCall.Builder convention(Linker.Option convention) {
            return this;
        }
        
        @Override
        public NativeCall.Builder critical() {
            return this;
        }
        
        @Override
        public Object call(Object... args) throws Throwable {
            throw new UnsupportedOperationException(
                "Native calls require AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public <T> NativeCall.Handle<T> prepare() {
            return new StubHandle<>();
        }
    }
    
    /**
     * Stub Handle implementation.
     */
    private static class StubHandle<T> implements NativeCall.Handle<T> {
        
        @Override
        public T call(Object... args) throws Throwable {
            throw new UnsupportedOperationException(
                "Native calls require AprismJDK. Running on stock JDK.");
        }
        
        @Override
        public MethodHandle methodHandle() {
            return null;
        }
        
        @Override
        public FunctionDescriptor descriptor() {
            return null;
        }
    }
}
