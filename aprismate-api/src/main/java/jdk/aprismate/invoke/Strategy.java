package jdk.aprismate.invoke;

/**
 * How a direct accessor was ultimately constructed.
 */
public enum Strategy {
    /**
     * Hidden-class bytecode generation (fork-backed tier, v26.3 GA).
     */
    GENERATED,
    /**
     * MethodHandle obtained via {@code privateLookupIn}.
     */
    METHOD_HANDLE,
    /**
     * MethodHandle obtained after {@code setAccessible(true)}.
     */
    METHOD_HANDLE_SET_ACCESSIBLE,
    /**
     * Plain {@code Method.invoke}/{@code Field.get} bridge.
     */
    PLAIN_REFLECTIVE
}
