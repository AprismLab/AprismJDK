/**
 * Provides asynchronous programming utilities.
 * <p>
 * The async API provides Promise-based utilities for asynchronous operations:
 * <ul>
 *   <li>{@link Promise} - Promise-based async operations</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * Promise<String> promise = Promise.resolve("Hello")
 *     .then(value -> value + " World")
 *     .then(result -> System.out.println(result))
 *     .catchError(err -> System.err.println("Error: " + err));
 * }</pre>
 * 
 * @since 26.1
 */
package jdk.aprismate.async;
