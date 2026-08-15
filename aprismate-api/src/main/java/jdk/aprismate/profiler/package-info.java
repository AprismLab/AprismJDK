/**
 * Low-overhead performance profiling APIs.
 * 
 * <p>This package provides comprehensive profiling capabilities with minimal
 * performance impact using async-profiler integration:
 * <ul>
 *   <li>CPU profiling (sampling-based, 1-5% overhead)</li>
 *   <li>Allocation profiling (TLAB-based, <1% overhead)</li>
 *   <li>Wall-clock profiling (all threads including waiting)</li>
 *   <li>Lock contention profiling</li>
 *   <li>Flamegraph generation</li>
 *   <li>JFR export</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>CPU Profiling</h3>
 * <p>Sample-based profiling for finding CPU hotspots:
 * <pre>{@code
 * // Profile CPU usage
 * Profiler.cpu()
 *     .interval(Duration.ofMillis(10))  // 10ms sampling
 *     .includeNative()                  // Include JNI calls
 *     .start();
 * 
 * // Run application
 * runMinecraftServer();
 * 
 * // Analyze results
 * ProfileResult result = Profiler.stop();
 * result.saveFlamegraph(Path.of("cpu-profile.html"));
 * 
 * result.topMethods(10).forEach(m -> {
 *     System.out.printf("%s: %.2f%%\n", m.name(), m.percentage());
 * });
 * }</pre>
 * 
 * <h3>Allocation Profiling</h3>
 * <p>Track memory allocations with TLAB instrumentation:
 * <pre>{@code
 * // Profile allocations
 * Profiler.allocation()
 *     .threshold(1024)  // Only >1KB allocations
 *     .includeSize()    // Track allocation sizes
 *     .start();
 * 
 * runApplication();
 * 
 * ProfileResult result = Profiler.stop();
 * 
 * // Find allocation hotspots
 * result.topAllocations(10).forEach(alloc -> {
 *     System.out.printf("%s: %d MB (%d allocations)\n",
 *         alloc.type(),
 *         alloc.totalBytes() / 1024 / 1024,
 *         alloc.count()
 *     );
 * });
 * }</pre>
 * 
 * <h3>Lock Profiling</h3>
 * <p>Find lock contention issues:
 * <pre>{@code
 * // Profile locks
 * Profiler.lock()
 *     .threshold(Duration.ofMicros(100))  // >100μs waits
 *     .start();
 * 
 * runApplication();
 * 
 * ProfileResult result = Profiler.stop();
 * 
 * result.topLocks(10).forEach(lock -> {
 *     System.out.printf("%s: %d ms total, %.2f ms avg\n",
 *         lock.monitor(),
 *         lock.totalWaitTime().toMillis(),
 *         lock.averageWaitTime().toNanos() / 1_000_000.0
 *     );
 * });
 * }</pre>
 * 
 * <h2>Complete Example - Minecraft Server Performance Analysis</h2>
 * <pre>{@code
 * public class MinecraftProfiler {
 *     
 *     public static void main(String[] args) throws Exception {
 *         System.out.println("Starting Minecraft server with profiling...");
 *         
 *         // Start server in background
 *         Thread serverThread = new Thread(() -> {
 *             MinecraftServer.start();
 *         });
 *         serverThread.start();
 *         
 *         // Wait for server startup
 *         Thread.sleep(30_000);
 *         
 *         // Profile CPU for 60 seconds
 *         System.out.println("Profiling CPU...");
 *         Profiler.cpu()
 *             .interval(Duration.ofMillis(10))
 *             .threads(t -> !t.getName().startsWith("netty"))  // Exclude netty
 *             .start();
 *         
 *         Thread.sleep(60_000);
 *         
 *         ProfileResult cpuResult = Profiler.stop();
 *         cpuResult.saveFlamegraph(Path.of("minecraft-cpu.html"));
 *         
 *         System.out.println("\n=== CPU Hotspots ===");
 *         cpuResult.topMethods(20).forEach(m -> {
 *             System.out.printf("%.2f%% - %s\n", m.percentage(), m.name());
 *         });
 *         
 *         // Profile allocations
 *         System.out.println("\nProfiling allocations...");
 *         Profiler.allocation()
 *             .threshold(4096)  // >4KB allocations
 *             .includeSize()
 *             .start();
 *         
 *         Thread.sleep(60_000);
 *         
 *         ProfileResult allocResult = Profiler.stop();
 *         allocResult.saveFlamegraph(Path.of("minecraft-alloc.html"));
 *         
 *         System.out.println("\n=== Allocation Hotspots ===");
 *         allocResult.topAllocations(20).forEach(alloc -> {
 *             System.out.printf("%d MB - %s (%d allocations)\n",
 *                 alloc.totalBytes() / 1024 / 1024,
 *                 alloc.type(),
 *                 alloc.count()
 *             );
 *         });
 *         
 *         // Profile locks
 *         System.out.println("\nProfiling lock contention...");
 *         Profiler.lock()
 *             .threshold(Duration.ofMillis(1))
 *             .start();
 *         
 *         Thread.sleep(60_000);
 *         
 *         ProfileResult lockResult = Profiler.stop();
 *         lockResult.saveFlamegraph(Path.of("minecraft-lock.html"));
 *         
 *         System.out.println("\n=== Lock Contention ===");
 *         lockResult.topLocks(20).forEach(lock -> {
 *             System.out.printf("%d ms total (%.2f ms avg) - %s\n",
 *                 lock.totalWaitTime().toMillis(),
 *                 lock.averageWaitTime().toNanos() / 1_000_000.0,
 *                 lock.monitor()
 *             );
 *         });
 *         
 *         System.out.println("\nProfiling complete. Check HTML files for flamegraphs.");
 *     }
 * }
 * }</pre>
 * 
 * <h2>Profiling Modes Comparison</h2>
 * <table border="1">
 *   <tr>
 *     <th>Mode</th>
 *     <th>What It Measures</th>
 *     <th>Overhead</th>
 *     <th>Best For</th>
 *   </tr>
 *   <tr>
 *     <td>CPU</td>
 *     <td>On-CPU time</td>
 *     <td>1-5%</td>
 *     <td>Finding compute hotspots</td>
 *   </tr>
 *   <tr>
 *     <td>Wall-clock</td>
 *     <td>All time (including waits)</td>
 *     <td>1-5%</td>
 *     <td>Finding blocking operations</td>
 *   </tr>
 *   <tr>
 *     <td>Allocation</td>
 *     <td>Memory allocations</td>
 *     <td>&lt;1%</td>
 *     <td>Finding GC pressure sources</td>
 *   </tr>
 *   <tr>
 *     <td>Lock</td>
 *     <td>Lock wait time</td>
 *     <td>&lt;1%</td>
 *     <td>Finding contention issues</td>
 *   </tr>
 * </table>
 * 
 * <h2>Integration with Standard JVM</h2>
 * <p>On stock JDK, these APIs gracefully degrade:
 * <ul>
 *   <li>All profiler builders work but throw UnsupportedOperationException on start()</li>
 *   <li>Use JFR or VisualVM instead for basic profiling on stock JDK</li>
 *   <li>AprismJDK provides much lower overhead and richer data</li>
 * </ul>
 * 
 * <h2>Performance Guidelines</h2>
 * 
 * <h3>Sampling Intervals</h3>
 * <ul>
 *   <li><b>1ms</b>: Maximum accuracy, 5-10% overhead, use for short profiles</li>
 *   <li><b>10ms</b>: Good balance, 1-2% overhead, recommended default</li>
 *   <li><b>100ms</b>: Low overhead, &lt;0.5%, use for production monitoring</li>
 * </ul>
 * 
 * <h3>When to Use Each Mode</h3>
 * <ul>
 *   <li><b>CPU</b>: Application feels slow, high CPU usage</li>
 *   <li><b>Wall-clock</b>: Application seems stuck, low CPU usage</li>
 *   <li><b>Allocation</b>: High GC overhead, frequent young GCs</li>
 *   <li><b>Lock</b>: Low throughput, many threads waiting</li>
 * </ul>
 * 
 * <h2>Output Formats</h2>
 * <ul>
 *   <li><b>Flamegraph HTML</b>: Interactive visualization, best for exploration</li>
 *   <li><b>JFR</b>: Java Flight Recorder format, open with JDK Mission Control</li>
 *   <li><b>Raw</b>: Collapsed stacks, use with external tools</li>
 * </ul>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <h3>Thread Filtering</h3>
 * <pre>{@code
 * // Only profile game logic threads
 * Profiler.cpu()
 *     .threads(t -> t.getName().startsWith("Server thread"))
 *     .start();
 * 
 * // Exclude I/O threads
 * Profiler.cpu()
 *     .threads(t -> !t.getName().contains("netty") && 
 *                   !t.getName().contains("IO"))
 *     .start();
 * }</pre>
 * 
 * <h3>Class Filtering</h3>
 * <pre>{@code
 * // Only profile mod allocations
 * Profiler.allocation()
 *     .classes(c -> c.getName().startsWith("com.mymod"))
 *     .start();
 * }</pre>
 * 
 * <h3>Native Stack Traces</h3>
 * <pre>{@code
 * // Include JNI and C/C++ frames
 * Profiler.cpu()
 *     .includeNative()
 *     .includeKernel()  // Also kernel (requires root)
 *     .start();
 * }</pre>
 * 
 * @since v26.0-Alpha.9
 * @author BlockConnect@StarsailsClover
 */
package jdk.aprismate.profiler;
