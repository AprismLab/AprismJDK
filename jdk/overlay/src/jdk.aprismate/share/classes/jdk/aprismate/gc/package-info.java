/**
 * Garbage Collector control and monitoring APIs.
 * 
 * <p>This package provides fine-grained control over GC behavior and
 * real-time monitoring capabilities beyond standard {@link java.lang.management}:
 * <ul>
 *   <li>Trigger specific GC phases (young, old, concurrent)</li>
 *   <li>Adjust GC parameters at runtime</li>
 *   <li>Monitor GC events in real-time</li>
 *   <li>Collect detailed GC statistics</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>GcController</h3>
 * <p>Main API for GC control:
 * <pre>{@code
 * // Trigger specific GC types
 * GcController.triggerYoungGc();       // Minor GC
 * GcController.triggerFullGc();        // Full GC
 * GcController.triggerConcurrentMark(); // Concurrent phase
 * 
 * // Runtime tuning
 * GcController.setConcurrentThreads(4);
 * GcController.setPauseTarget(Duration.ofMillis(10));
 * GcController.setYoungGenSize(512 * 1024 * 1024);
 * }</pre>
 * 
 * <h3>GC Monitoring</h3>
 * <p>Real-time event monitoring:
 * <pre>{@code
 * // Add listener for all GC events
 * GcController.addListener(event -> {
 *     System.out.printf("%s GC: %d ms, freed %d KB%n",
 *         event.type(),
 *         event.duration().toMillis(),
 *         event.freedBytes() / 1024
 *     );
 * });
 * 
 * // Get statistics
 * GcStats stats = GcController.getStats();
 * System.out.println("Total pauses: " + stats.totalPauses());
 * System.out.println("Average pause: " + stats.averagePauseTime());
 * System.out.println("P99 pause: " + stats.p99PauseTime());
 * System.out.println("GC overhead: " + stats.gcOverhead() + "%");
 * }</pre>
 * 
 * <h2>Complete Example - Minecraft Server Optimization</h2>
 * <pre>{@code
 * public class MinecraftGcTuner {
 *     
 *     public static void main(String[] args) {
 *         // Configure GC for low-latency gaming
 *         setupGc();
 *         
 *         // Monitor GC impact
 *         monitorGc();
 *         
 *         // Start server
 *         MinecraftServer.start();
 *     }
 *     
 *     private static void setupGc() {
 *         // Target 10ms max pause for 60 FPS (16.6ms frame time)
 *         GcController.setPauseTarget(Duration.ofMillis(10));
 *         
 *         // Use 4 concurrent threads on 8-core CPU
 *         GcController.setConcurrentThreads(4);
 *         
 *         // Large young gen for high allocation rate
 *         GcController.setYoungGenSize(2L * 1024 * 1024 * 1024); // 2 GB
 *         
 *         System.out.println("GC Type: " + GcController.gcType());
 *         System.out.println("Concurrent: " + GcController.supportsConcurrentMarking());
 *     }
 *     
 *     private static void monitorGc() {
 *         GcController.addListener(event -> {
 *             // Log long pauses that might cause lag
 *             if (event.duration().toMillis() > 50) {
 *                 System.err.printf("WARNING: Long GC pause: %d ms (%s)%n",
 *                     event.duration().toMillis(),
 *                     event.cause()
 *                 );
 *             }
 *         });
 *         
 *         // Periodic statistics reporting
 *         Timer timer = new Timer(true);
 *         timer.scheduleAtFixedRate(new TimerTask() {
 *             @Override
 *             public void run() {
 *                 reportStats();
 *             }
 *         }, 60000, 60000); // Every minute
 *     }
 *     
 *     private static void reportStats() {
 *         GcStats stats = GcController.getStats();
 *         
 *         System.out.printf("=== GC Stats (last minute) ===%n");
 *         System.out.printf("Young GCs: %d (avg %.1f ms)%n",
 *             stats.youngGcCount(),
 *             stats.youngGcTime().toMillis() / (double) stats.youngGcCount()
 *         );
 *         System.out.printf("Full GCs: %d%n", stats.fullGcCount());
 *         System.out.printf("P95 pause: %d ms%n", stats.p95PauseTime().toMillis());
 *         System.out.printf("P99 pause: %d ms%n", stats.p99PauseTime().toMillis());
 *         System.out.printf("Heap: %.1f%% used (%d / %d MB)%n",
 *             stats.heapUtilization(),
 *             stats.heapUsed() / 1024 / 1024,
 *             stats.heapCapacity() / 1024 / 1024
 *         );
 *         System.out.printf("Allocation rate: %d MB/s%n",
 *             stats.allocationRate() / 1024 / 1024
 *         );
 *         System.out.printf("GC overhead: %.2f%%%n", stats.gcOverhead());
 *         
 *         // Reset for next period
 *         stats.reset();
 *     }
 * }
 * }</pre>
 * 
 * <h2>GC Type Support Matrix</h2>
 * <table border="1">
 *   <tr>
 *     <th>Feature</th>
 *     <th>G1GC</th>
 *     <th>ZGC</th>
 *     <th>Shenandoah</th>
 *     <th>Parallel</th>
 *     <th>Serial</th>
 *   </tr>
 *   <tr>
 *     <td>Young GC trigger</td>
 *     <td>yes</td>
 *     <td>N/A</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *   </tr>
 *   <tr>
 *     <td>Concurrent mark trigger</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>no</td>
 *     <td>no</td>
 *   </tr>
 *   <tr>
 *     <td>Concurrent compact trigger</td>
 *     <td>no</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>no</td>
 *     <td>no</td>
 *   </tr>
 *   <tr>
 *     <td>Thread count adjustment</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>N/A</td>
 *   </tr>
 *   <tr>
 *     <td>Pause target</td>
 *     <td>yes</td>
 *     <td>no</td>
 *     <td>yes</td>
 *     <td>no</td>
 *     <td>no</td>
 *   </tr>
 *   <tr>
 *     <td>Generation sizing</td>
 *     <td>yes</td>
 *     <td>N/A</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *     <td>yes</td>
 *   </tr>
 * </table>
 * 
 * <h2>Performance Guidelines</h2>
 * 
 * <h3>For Minecraft Servers (Low Latency)</h3>
 * <ul>
 *   <li>Use G1GC or ZGC</li>
 *   <li>Set pause target to 5-10ms</li>
 *   <li>Large young generation (2-4GB for 16GB heap)</li>
 *   <li>4 concurrent threads on 8+ core CPUs</li>
 *   <li>Monitor P99 pause times</li>
 * </ul>
 * 
 * <h3>For High Throughput Applications</h3>
 * <ul>
 *   <li>Use Parallel GC or G1GC</li>
 *   <li>Maximize parallel thread count</li>
 *   <li>Allow longer pause times (50-100ms)</li>
 *   <li>Focus on total GC time, not pause times</li>
 * </ul>
 * 
 * <h3>For Batch Processing</h3>
 * <ul>
 *   <li>Use Parallel GC</li>
 *   <li>Disable explicit GC if not needed</li>
 *   <li>Large heap to reduce GC frequency</li>
 *   <li>Monitor GC overhead (should be <5%)</li>
 * </ul>
 * 
 * <h2>Integration with Standard JVM</h2>
 * <p>On stock JDK, these APIs provide graceful degradation:
 * <ul>
 *   <li>triggerYoungGc() / triggerFullGc() fall back to System.gc()</li>
 *   <li>Advanced features throw UnsupportedOperationException</li>
 *   <li>GcStats returns basic info from Runtime/MemoryMXBean</li>
 *   <li>Event listeners work but receive no events</li>
 * </ul>
 * 
 * @since v26.0-Alpha.9
 * @author BlockConnect@StarsailsClover
 */
package jdk.aprismate.gc;
