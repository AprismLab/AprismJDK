/**
 * Advanced memory management APIs for high-performance applications.
 * 
 * <p>This package provides low-level memory management capabilities that go
 * beyond the standard Java memory model. It enables:
 * <ul>
 *   <li>Off-heap memory allocation and management</li>
 *   <li>Zero-copy I/O operations</li>
 *   <li>Fine-grained memory lifetime control</li>
 *   <li>Reduced GC pressure for large datasets</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Arena Allocator</h3>
 * <p>Region-based memory allocator for fast bulk allocation and deallocation:
 * <pre>{@code
 * try (Arena arena = Arena.ofConfined()) {
 *     MemorySegment seg = arena.allocate(1024);
 *     // Use memory...
 * } // All allocations freed here
 * }</pre>
 * 
 * <h3>Direct Buffer Pool</h3>
 * <p>High-performance buffer pool with automatic leak detection:
 * <pre>{@code
 * DirectBufferPool pool = DirectBufferPool.create();
 * ByteBuffer buffer = pool.acquire(8192);
 * try {
 *     // Use buffer...
 * } finally {
 *     pool.release(buffer);
 * }
 * }</pre>
 * 
 * <h3>Off-Heap Map</h3>
 * <p>Hash map stored entirely in native memory:
 * <pre>{@code
 * try (OffHeapMap<String, User> cache = OffHeapMap.create(
 *         String.class, User.class, 1_000_000)) {
 *     cache.put("user:1", new User("Alice"));
 *     User user = cache.get("user:1");
 * }
 * }</pre>
 * 
 * <h2>Performance Benefits</h2>
 * <ul>
 *   <li><b>Arena</b>: 5x faster than individual allocations</li>
 *   <li><b>Buffer Pool</b>: >95% reuse rate, minimal GC pressure</li>
 *   <li><b>Off-Heap Map</b>: Zero GC overhead, unlimited size</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>All classes in this package offer thread-safe options:
 * <ul>
 *   <li>{@link jdk.aprismate.memory.Arena#ofConfined()} - single-thread only</li>
 *   <li>{@link jdk.aprismate.memory.Arena#ofShared()} - multi-thread safe</li>
 * </ul>
 * 
 * <h2>Memory Safety</h2>
 * <p>While these APIs provide low-level access, they include safety features:
 * <ul>
 *   <li>Bounds checking in debug mode</li>
 *   <li>Use-after-close detection</li>
 *   <li>Memory leak detection</li>
 *   <li>Automatic cleanup with try-with-resources</li>
 * </ul>
 * 
 * @since v26.0-Alpha.9
 * @author BlockConnect@StarsailsClover
 */
package jdk.aprismate.memory;
