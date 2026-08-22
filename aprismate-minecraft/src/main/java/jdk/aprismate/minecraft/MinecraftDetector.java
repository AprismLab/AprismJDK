package jdk.aprismate.minecraft;

/**
 * Detects whether the current JVM hosts a Minecraft runtime and, when
 * possible, its loader family, side, and version.
 *
 * <p>Implementations are fail-safe: {@link #detect()} never throws and
 * returns a result with {@code detected == false} on stock JVMs.
 */
public interface MinecraftDetector {

    MinecraftRuntime detect();

    default boolean shouldOptimize() {
        return detect().detected();
    }
}
