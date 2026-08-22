/**
 * Reflection-elimination framework: cached MethodHandle-based direct
 * invokers and field accessors replacing hot-path {@code Method.invoke}
 * and {@code Field.get}. Tiered degradation keeps stock-JDK safety;
 * a generated-bytecode tier (hidden classes) is planned for the fork
 * line.
 */
package jdk.aprismate.invoke;
