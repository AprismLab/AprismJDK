package jdk.aprismate.ffi;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.*;

/**
 * Factory for StructLayout instances.
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
final class StructLayoutFactory {
    
    private StructLayoutFactory() {
        // No instantiation
    }
    
    static StructLayout.Builder define(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        
        try {
            Class<?> implClass = Class.forName("com.aprismate.agent.ffi.StructLayoutImpl");
            return (StructLayout.Builder) implClass.getMethod("define", String.class)
                .invoke(null, name);
        } catch (Exception e) {
            return new StubBuilder(name);
        }
    }
    
    /**
     * Stub Builder implementation.
     */
    private static class StubBuilder implements StructLayout.Builder {
        
        private final String name;
        private final List<MemoryLayout> fields = new ArrayList<>();
        
        StubBuilder(String name) {
            this.name = name;
        }
        
        @Override
        public StructLayout.Builder field(String name, MemoryLayout layout) {
            fields.add(layout.withName(name));
            return this;
        }
        
        @Override
        public StructLayout.Builder padding(int bytes) {
            fields.add(MemoryLayout.paddingLayout(bytes));
            return this;
        }
        
        @Override
        public StructLayout.Builder nested(String name, StructLayout struct) {
            fields.add(struct.layout().withName(name));
            return this;
        }
        
        @Override
        public StructLayout.Builder array(String name, MemoryLayout elementLayout, int count) {
            fields.add(MemoryLayout.sequenceLayout(count, elementLayout).withName(name));
            return this;
        }
        
        @Override
        public StructLayout.Builder autoPadding() {
            // No-op in stub
            return this;
        }
        
        @Override
        public StructLayout.Builder alignment(int alignment) {
            // No-op in stub
            return this;
        }
        
        @Override
        public StructLayout.Builder packed() {
            // No-op in stub
            return this;
        }
        
        @Override
        public StructLayout build() {
            GroupLayout layout = MemoryLayout.structLayout(fields.toArray(new MemoryLayout[0]))
                .withName(name);
            return new StubStructLayout(name, layout);
        }
    }
    
    /**
     * Stub StructLayout implementation.
     */
    private static class StubStructLayout implements StructLayout {
        
        private final String name;
        private final GroupLayout layout;
        private final Map<String, VarHandle> handles = new HashMap<>();
        
        StubStructLayout(String name, GroupLayout layout) {
            this.name = name;
            this.layout = layout;
        }
        
        @Override
        public String name() {
            return name;
        }
        
        @Override
        public GroupLayout layout() {
            return layout;
        }
        
        @Override
        public long size() {
            return layout.byteSize();
        }
        
        @Override
        public long alignment() {
            return layout.byteAlignment();
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(MemorySegment segment, String fieldName) {
            VarHandle handle = varHandle(fieldName);
            return (T) handle.get(segment, 0L);
        }
        
        @Override
        public void set(MemorySegment segment, String fieldName, Object value) {
            VarHandle handle = varHandle(fieldName);
            handle.set(segment, 0L, value);
        }
        
        @Override
        public VarHandle varHandle(String fieldName) {
            return handles.computeIfAbsent(fieldName, name -> {
                try {
                    return layout.varHandle(MemoryLayout.PathElement.groupElement(name));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Field not found: " + name, e);
                }
            });
        }
        
        @Override
        public long offset(String fieldName) {
            try {
                return layout.byteOffset(MemoryLayout.PathElement.groupElement(fieldName));
            } catch (Exception e) {
                throw new IllegalArgumentException("Field not found: " + fieldName, e);
            }
        }
    }
}
