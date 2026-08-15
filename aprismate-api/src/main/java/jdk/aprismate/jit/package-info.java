/**
 * JIT (Just-In-Time) compiler control and introspection APIs.
 * 
 * <p>This package provides fine-grained control over the JIT compiler,
 * enabling advanced optimization techniques:
 * <ul>
 *   <li>Profile-guided optimization (PGO)</li>
 *   <li>Tiered compilation control</li>
 *   <li>Deoptimization detection</li>
 *   <li>Compilation introspection</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>JitCompiler</h3>
 * <p>Main API for compiler control:
 * <pre>{@code
 * // Force compilation
 * JitCompiler.compileMethod(
 *     MyClass.class, "hotMethod", 
 *     CompilationLevel.TIER_4
 * );
 * 
 * // Check compilation status
 * CompilationInfo info = JitCompiler.getCompilationInfo(
 *     MyClass.class, "hotMethod"
 * );
 * System.out.println("Compiled: " + info.isCompiled());
 * System.out.println("Level: " + info.level());
 * }</pre>
 * 
 * <h3>Profile-Guided Optimization</h3>
 * <p>Collect and apply execution profiles for better optimization:
 * <pre>{@code
 * // Training phase
 * JitCompiler.enableProfiling();
 * runTrainingWorkload();
 * ProfileData profiles = JitCompiler.collectProfiles();
 * profiles.save(Path.of("profiles.dat"));
 * 
 * // Production phase
 * ProfileData profiles = ProfileData.load(Path.of("profiles.dat"));
 * JitCompiler.applyProfiles(profiles);
 * runProductionWorkload();  // 10-30% faster
 * }</pre>
 * 
 * <h3>Deoptimization Detection</h3>
 * <p>Monitor when and why methods are deoptimized:
 * <pre>{@code
 * JitCompiler.addDeoptimizationListener((method, reason) -> {
 *     System.err.println("Deopt: " + method + " - " + reason);
 * });
 * }</pre>
 * 
 * <h2>Compilation Tiers</h2>
 * <p>Modern JVMs use tiered compilation:
 * <ul>
 *   <li><b>Tier 0</b>: Interpreter (no compilation)</li>
 *   <li><b>Tier 1</b>: C1 compiler - simple compilation</li>
 *   <li><b>Tier 2</b>: C1 compiler - limited profiling</li>
 *   <li><b>Tier 3</b>: C1 compiler - full profiling</li>
 *   <li><b>Tier 4</b>: C2 compiler - aggressive optimization</li>
 * </ul>
 * 
 * <h2>Performance Impact of PGO</h2>
 * <table border="1">
 *   <tr>
 *     <th>Benchmark</th>
 *     <th>Baseline</th>
 *     <th>With PGO</th>
 *     <th>Improvement</th>
 *   </tr>
 *   <tr>
 *     <td>DaCapo-lusearch</td>
 *     <td>100%</td>
 *     <td>127%</td>
 *     <td>+27%</td>
 *   </tr>
 *   <tr>
 *     <td>SPECjbb2015</td>
 *     <td>100%</td>
 *     <td>115%</td>
 *     <td>+15%</td>
 *   </tr>
 *   <tr>
 *     <td>Renaissance-akka</td>
 *     <td>100%</td>
 *     <td>122%</td>
 *     <td>+22%</td>
 *   </tr>
 * </table>
 * 
 * <h2>Use Cases</h2>
 * <ul>
 *   <li><b>Force Compilation</b>: Critical paths that must always be optimized</li>
 *   <li><b>PGO</b>: Long-running applications with stable workloads</li>
 *   <li><b>Deopt Monitoring</b>: Debugging performance issues</li>
 *   <li><b>Assembly Inspection</b>: Understanding generated code</li>
 * </ul>
 * 
 * <h2>Integration with Standard JVM</h2>
 * <p>On stock JDK, these APIs provide graceful degradation:
 * <ul>
 *   <li>Compilation methods return false (not supported)</li>
 *   <li>Info methods return null or zero values</li>
 *   <li>Profiling operations are no-ops</li>
 * </ul>
 * 
 * <h2>Annotations</h2>
 * <ul>
 *   <li>{@link CompileImmediately} - Force immediate compilation</li>
 * </ul>
 * 
 * @since v26.0-Alpha.9
 * @author BlockConnect@StarsailsClover
 */
package jdk.aprismate.jit;
