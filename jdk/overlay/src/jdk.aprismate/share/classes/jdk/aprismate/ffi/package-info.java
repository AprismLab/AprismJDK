/**
 * Foreign Function Interface (FFI) enhancements for native interop.
 * 
 * <p>This package extends the standard {@link java.lang.foreign} API
 * with additional features for easier and more powerful native code integration:
 * <ul>
 *   <li>Enhanced library loading with symbol versioning</li>
 *   <li>Fluent API for native function calls</li>
 *   <li>Convenient struct layout definition</li>
 *   <li>Cross-platform compatibility helpers</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>NativeLibrary</h3>
 * <p>Enhanced library loading with automatic path discovery:
 * <pre>{@code
 * // Load a library
 * NativeLibrary lib = NativeLibrary.load("mylib");
 * 
 * // Find symbols
 * Optional<MemorySegment> symbol = lib.find("my_function");
 * 
 * // Symbol versioning (Linux)
 * Optional<MemorySegment> versioned = lib.find("memcpy", "GLIBC_2.17");
 * 
 * // List all symbols
 * String[] symbols = lib.symbols();
 * }</pre>
 * 
 * <h3>NativeCall</h3>
 * <p>Fluent API for calling native functions:
 * <pre>{@code
 * NativeLibrary libc = NativeLibrary.cLibrary();
 * 
 * // Simple call
 * long length = NativeCall.to(libc, "strlen")
 *     .returns(long.class)
 *     .args(MemorySegment.class)
 *     .call(stringSegment);
 * 
 * // Reusable handle
 * NativeCall.Handle<Integer> open = NativeCall.to(libc, "open")
 *     .returns(int.class)
 *     .args(MemorySegment.class, int.class, int.class)
 *     .prepare();
 * 
 * int fd = open.call(pathSegment, O_RDONLY, 0);
 * }</pre>
 * 
 * <h3>StructLayout</h3>
 * <p>Convenient struct definition and field access:
 * <pre>{@code
 * // Define struct Point { int x; int y; }
 * StructLayout pointLayout = StructLayout.define("Point")
 *     .field("x", ValueLayout.JAVA_INT)
 *     .field("y", ValueLayout.JAVA_INT)
 *     .build();
 * 
 * // Allocate and access
 * try (Arena arena = Arena.ofConfined()) {
 *     MemorySegment point = pointLayout.allocate(arena);
 *     
 *     pointLayout.set(point, "x", 10);
 *     pointLayout.set(point, "y", 20);
 *     
 *     int x = pointLayout.get(point, "x");
 *     int y = pointLayout.get(point, "y");
 * }
 * }</pre>
 * 
 * <h2>Complete Example - OpenGL Integration</h2>
 * <pre>{@code
 * // Load OpenGL library
 * NativeLibrary gl = NativeLibrary.builder()
 *     .name("GL")
 *     .searchPath("/usr/lib/x86_64-linux-gnu")
 *     .load();
 * 
 * // Define vertex struct
 * StructLayout vertex = StructLayout.define("Vertex")
 *     .field("x", ValueLayout.JAVA_FLOAT)
 *     .field("y", ValueLayout.JAVA_FLOAT)
 *     .field("z", ValueLayout.JAVA_FLOAT)
 *     .build();
 * 
 * // Create call handles
 * NativeCall.Handle<Void> glBegin = NativeCall.to(gl, "glBegin")
 *     .returnsVoid()
 *     .args(int.class)
 *     .prepare();
 * 
 * NativeCall.Handle<Void> glVertex3fv = NativeCall.to(gl, "glVertex3fv")
 *     .returnsVoid()
 *     .args(MemorySegment.class)
 *     .prepare();
 * 
 * NativeCall.Handle<Void> glEnd = NativeCall.to(gl, "glEnd")
 *     .returnsVoid()
 *     .args()
 *     .prepare();
 * 
 * // Render
 * try (Arena arena = Arena.ofConfined()) {
 *     MemorySegment v1 = vertex.allocate(arena);
 *     vertex.set(v1, "x", 0.0f);
 *     vertex.set(v1, "y", 1.0f);
 *     vertex.set(v1, "z", 0.0f);
 *     
 *     glBegin.call(GL_TRIANGLES);
 *     glVertex3fv.call(v1);
 *     // ... more vertices
 *     glEnd.call();
 * }
 * }</pre>
 * 
 * <h2>Cross-Platform Support</h2>
 * <p>Library names are automatically mapped:
 * <table border="1">
 *   <tr>
 *     <th>Platform</th>
 *     <th>Input</th>
 *     <th>Resolved</th>
 *   </tr>
 *   <tr>
 *     <td>Linux</td>
 *     <td>"mylib"</td>
 *     <td>libmylib.so</td>
 *   </tr>
 *   <tr>
 *     <td>macOS</td>
 *     <td>"mylib"</td>
 *     <td>libmylib.dylib</td>
 *   </tr>
 *   <tr>
 *     <td>Windows</td>
 *     <td>"mylib"</td>
 *     <td>mylib.dll</td>
 *   </tr>
 * </table>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li><b>Critical calls</b>: Use {@code .critical()} for hot paths that don't callback to Java</li>
 *   <li><b>Reusable handles</b>: Use {@code .prepare()} to create handles for repeated calls</li>
 *   <li><b>Arena allocation</b>: Use confined arenas for better performance in single-threaded code</li>
 * </ul>
 * 
 * <h2>Safety Notes</h2>
 * <ul>
 *   <li>Always use try-with-resources for Arena to prevent memory leaks</li>
 *   <li>Never use MemorySegment after its Arena is closed</li>
 *   <li>Critical calls cannot call back into Java or block</li>
 *   <li>Symbol addresses become invalid after library unload</li>
 * </ul>
 * 
 * <h2>Integration with Standard JVM</h2>
 * <p>On stock JDK, these APIs provide graceful degradation:
 * <ul>
 *   <li>Basic StructLayout works (backed by standard MemoryLayout)</li>
 *   <li>Advanced features (symbol versioning, lazy loading) throw UnsupportedOperationException</li>
 *   <li>NativeLibrary.load() uses standard System.loadLibrary()</li>
 * </ul>
 * 
 * @since v26.0-Alpha.9
 * @author BlockConnect@StarsailsClover
 */
package jdk.aprismate.ffi;
