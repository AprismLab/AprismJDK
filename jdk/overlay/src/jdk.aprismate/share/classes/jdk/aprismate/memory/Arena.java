package jdk.aprismate.memory;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;

/**
 * Arena - Fast bulk memory allocator with automatic lifetime management.
 * 
 * <p>Arena allocators provide a region-based memory management model where
 * all allocations within an arena are freed together when the arena is closed.
 * This eliminates individual deallocation overhead and improves cache locality.
 * 
 * <h2>Thread Safety Models</h2>
 * <ul>
 *   <li><b>Confined</b>: Single-thread access, no synchronization overhead</li>
 *   <li><b>Shared</b>: Multi-thread access, thread-safe with minimal contention</li>
 *   <li><b>Auto</b>: Scoped lifetime, cleaned up automatically</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * try (Arena arena = Arena.ofConfined()) {
 *     MemorySegment seg1 = arena.allocate(1024);
 *     MemorySegment seg2 = arena.allocate(String.class, 100);
 *     
 *     // Use memory segments...
 *     
 *     System.out.println("Allocated: " + arena.allocated() + " bytes");
 * } // All allocations freed here
 * }</pre>
 * 
 * <h2>Performance Characteristics</h2>
 * <ul>
 *   <li>Allocation: O(1), ~10ns per allocation</li>
 *   <li>Deallocation: O(1), bulk free in ~1us</li>
 *   <li>Memory overhead: <1% for typical workloads</li>
 * </ul>
 * 
 * @author BlockConnect@StarsailsClover
 * @since v26.0-Alpha.9
 * @see MemorySegment
 * @see SegmentAllocator
 */
public interface Arena extends AutoCloseable {
    
    /**
     * Allocates a memory segment of the specified size.
     * 
     * <p>The returned segment is owned by this arena and will be freed
     * when the arena is closed. The memory is uninitialized.
     * 
     * @param size the size in bytes, must be positive
     * @return a memory segment of the requested size
     * @throws IllegalArgumentException if size is negative or zero
     * @throws IllegalStateException if the arena is closed
     * @throws OutOfMemoryError if allocation fails
     */
    MemorySegment allocate(long size);
    
    /**
     * Allocates a memory segment with specified size and alignment.
     * 
     * <p>The returned segment's address will be aligned to the specified
     * alignment, which must be a power of two.
     * 
     * @param size the size in bytes, must be positive
     * @param alignment the alignment in bytes, must be a power of two
     * @return an aligned memory segment
     * @throws IllegalArgumentException if size or alignment is invalid
     * @throws IllegalStateException if the arena is closed
     * @throws OutOfMemoryError if allocation fails
     */
    MemorySegment allocate(long size, long alignment);
    
    /**
     * Allocates memory for an array of typed elements.
     * 
     * <p>The size is calculated as {@code elementSize * count}, where
     * elementSize is determined from the type. For primitives, standard
     * sizes are used. For objects, implementation-specific sizing applies.
     * 
     * @param <T> the element type
     * @param type the class object representing the element type
     * @param count the number of elements, must be positive
     * @return a memory segment sized for the array
     * @throws IllegalArgumentException if count is negative or type is null
     * @throws IllegalStateException if the arena is closed
     * @throws OutOfMemoryError if allocation fails
     */
    <T> MemorySegment allocate(Class<T> type, long count);
    
    /**
     * Allocates and initializes a memory segment from a byte array.
     * 
     * <p>The returned segment contains a copy of the array data.
     * 
     * @param bytes the byte array to copy
     * @return a memory segment containing the array data
     * @throws NullPointerException if bytes is null
     * @throws IllegalStateException if the arena is closed
     * @throws OutOfMemoryError if allocation fails
     */
    MemorySegment allocateFrom(byte[] bytes);
    
    /**
     * Returns the total number of bytes allocated in this arena.
     * 
     * <p>This includes all allocations made since the arena was created,
     * including internal overhead and alignment padding.
     * 
     * @return the total allocated bytes
     */
    long allocated();
    
    /**
     * Returns the peak memory usage of this arena.
     * 
     * <p>This is the maximum value of {@link #allocated()} since the
     * arena was created.
     * 
     * @return the peak allocated bytes
     */
    long peak();
    
    /**
     * Returns the number of allocations made in this arena.
     * 
     * @return the allocation count
     */
    long allocationCount();
    
    /**
     * Checks if this arena is closed.
     * 
     * @return true if closed, false otherwise
     */
    boolean isClosed();
    
    /**
     * Closes this arena and frees all allocated memory.
     * 
     * <p>After closing, all memory segments allocated from this arena
     * become invalid and must not be accessed. Attempting to access them
     * will result in {@link IllegalStateException}.
     * 
     * <p>This method is idempotent - calling it multiple times has no effect.
     * 
     * @throws IllegalStateException if called from a different thread than
     *         the one that created a confined arena
     */
    @Override
    void close();
    
    /**
     * Creates a confined arena.
     * 
     * <p>A confined arena can only be accessed from the thread that created it.
     * This provides the best performance as no synchronization is needed.
     * Attempting to access from another thread will throw {@link IllegalStateException}.
     * 
     * <h3>Performance</h3>
     * <ul>
     *   <li>Allocation: ~10ns</li>
     *   <li>No synchronization overhead</li>
     *   <li>Best for single-threaded workloads</li>
     * </ul>
     * 
     * @return a new confined arena
     */
    static Arena ofConfined() {
        return ArenaFactory.createConfined();
    }
    
    /**
     * Creates a shared arena.
     * 
     * <p>A shared arena can be accessed from multiple threads concurrently.
     * It uses lock-free algorithms to minimize contention and provide
     * scalable performance.
     * 
     * <h3>Performance</h3>
     * <ul>
     *   <li>Allocation: ~20-30ns</li>
     *   <li>Lock-free synchronization</li>
     *   <li>Scales well with thread count</li>
     * </ul>
     * 
     * @return a new shared arena
     */
    static Arena ofShared() {
        return ArenaFactory.createShared();
    }
    
    /**
     * Creates an auto-managed arena.
     * 
     * <p>An auto arena is automatically closed when it becomes unreachable
     * and is collected by the garbage collector. This provides convenience
     * at the cost of non-deterministic cleanup timing.
     * 
     * <p><b>Warning</b>: Auto arenas should only be used when deterministic
     * cleanup is not required. For most use cases, confined or shared arenas
     * with explicit {@link #close()} calls are preferred.
     * 
     * @return a new auto-managed arena
     */
    static Arena ofAuto() {
        return ArenaFactory.createAuto();
    }
    
    /**
     * Returns the global arena.
     * 
     * <p>The global arena is never closed and allocations from it are never
     * freed. It should only be used for data that needs to live for the
     * entire application lifetime.
     * 
     * <p><b>Warning</b>: Memory allocated from the global arena is never
     * reclaimed. Use with caution to avoid memory leaks.
     * 
     * @return the global arena instance
     */
    static Arena global() {
        return ArenaFactory.getGlobal();
    }
}
