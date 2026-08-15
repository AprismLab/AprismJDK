package jdk.aprismate.ffi;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

/**
 * StructLayout - Fluent API for native struct layout definition.
 * 
 * <p>This provides a more convenient interface than manually creating
 * MemoryLayout objects with offsets.
 * 
 * <h2>Usage Example - Simple Struct</h2>
 * <pre>{@code
 * // Define struct Point { int x; int y; }
 * StructLayout pointLayout = StructLayout.define("Point")
 *     .field("x", ValueLayout.JAVA_INT)
 *     .field("y", ValueLayout.JAVA_INT)
 *     .build();
 * 
 * // Allocate and access
 * try (Arena arena = Arena.ofConfined()) {
 *     MemorySegment point = arena.allocate(pointLayout.layout());
 *     
 *     pointLayout.set(point, "x", 10);
 *     pointLayout.set(point, "y", 20);
 *     
 *     int x = pointLayout.get(point, "x");
 *     int y = pointLayout.get(point, "y");
 * }
 * }</pre>
 * 
 * <h2>Usage Example - Complex Struct</h2>
 * <pre>{@code
 * // struct Person {
 * //     char name[64];
 * //     int age;
 * //     double salary;
 * //     Point location;
 * // }
 * StructLayout personLayout = StructLayout.define("Person")
 *     .field("name", MemoryLayout.sequenceLayout(64, ValueLayout.JAVA_BYTE))
 *     .field("age", ValueLayout.JAVA_INT)
 *     .field("salary", ValueLayout.JAVA_DOUBLE)
 *     .field("location", pointLayout.layout())
 *     .build();
 * }</pre>
 * 
 * <h2>Usage Example - With Padding</h2>
 * <pre>{@code
 * // struct Aligned {
 * //     char a;
 * //     // 3 bytes padding
 * //     int b;
 * // }
 * StructLayout aligned = StructLayout.define("Aligned")
 *     .field("a", ValueLayout.JAVA_BYTE)
 *     .padding(3)
 *     .field("b", ValueLayout.JAVA_INT)
 *     .build();
 * }</pre>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 */
public interface StructLayout {
    
    /**
     * Returns the struct name.
     * 
     * @return struct name
     */
    String name();
    
    /**
     * Returns the memory layout.
     * 
     * @return the layout
     */
    GroupLayout layout();
    
    /**
     * Returns the struct size in bytes.
     * 
     * @return size in bytes
     */
    long size();
    
    /**
     * Returns the struct alignment in bytes.
     * 
     * @return alignment in bytes
     */
    long alignment();
    
    /**
     * Gets a field value.
     * 
     * @param <T> the value type
     * @param segment the memory segment
     * @param fieldName the field name
     * @return the field value
     */
    <T> T get(MemorySegment segment, String fieldName);
    
    /**
     * Sets a field value.
     * 
     * @param segment the memory segment
     * @param fieldName the field name
     * @param value the value to set
     */
    void set(MemorySegment segment, String fieldName, Object value);
    
    /**
     * Gets the VarHandle for a field.
     * 
     * @param fieldName the field name
     * @return the var handle
     */
    VarHandle varHandle(String fieldName);
    
    /**
     * Gets the offset of a field.
     * 
     * @param fieldName the field name
     * @return the offset in bytes
     */
    long offset(String fieldName);
    
    /**
     * Allocates a new instance in the given arena.
     * 
     * @param arena the memory arena
     * @return allocated memory segment
     */
    default MemorySegment allocate(Arena arena) {
        return arena.allocate(layout());
    }
    
    /**
     * Allocates an array in the given arena.
     * 
     * @param arena the memory arena
     * @param count the array size
     * @return allocated memory segment
     */
    default MemorySegment allocateArray(Arena arena, int count) {
        long totalSize = layout().byteSize() * count;
        return arena.allocate(totalSize, layout().byteAlignment());
    }
    
    /**
     * Starts defining a new struct layout.
     * 
     * @param name the struct name
     * @return a new builder
     */
    static Builder define(String name) {
        return StructLayoutFactory.define(name);
    }
    
    /**
     * Builder for struct layouts.
     */
    interface Builder {
        
        /**
         * Adds a field.
         * 
         * @param name the field name
         * @param layout the field layout
         * @return this builder
         */
        Builder field(String name, MemoryLayout layout);
        
        /**
         * Adds padding bytes.
         * 
         * @param bytes the number of padding bytes
         * @return this builder
         */
        Builder padding(int bytes);
        
        /**
         * Adds a nested struct.
         * 
         * @param name the field name
         * @param struct the nested struct layout
         * @return this builder
         */
        Builder nested(String name, StructLayout struct);
        
        /**
         * Adds an array field.
         * 
         * @param name the field name
         * @param elementLayout the element layout
         * @param count the array size
         * @return this builder
         */
        Builder array(String name, MemoryLayout elementLayout, int count);
        
        /**
         * Enables automatic padding for alignment.
         * 
         * <p>When enabled, padding bytes are automatically inserted
         * to align fields according to their natural alignment.
         * 
         * @return this builder
         */
        Builder autoPadding();
        
        /**
         * Sets explicit struct alignment.
         * 
         * @param alignment the alignment in bytes (must be power of 2)
         * @return this builder
         */
        Builder alignment(int alignment);
        
        /**
         * Enables packed layout (no padding).
         * 
         * @return this builder
         */
        Builder packed();
        
        /**
         * Builds the struct layout.
         * 
         * @return the struct layout
         */
        StructLayout build();
    }
}
