package jdk.aprismate.minecraft;

/**
 * Access point for the detection API. Returns the shared detector;
 * construction is lazy and failures degrade to a not-detected stub.
 */
public final class MinecraftDetectors {

    private static volatile MinecraftDetector instance;

    private MinecraftDetectors() {
    }

    public static MinecraftDetector getDetector() {
        MinecraftDetector d = instance;
        if (d == null) {
            synchronized (MinecraftDetectors.class) {
                d = instance;
                if (d == null) {
                    instance = d = new DefaultMinecraftDetector();
                }
            }
        }
        return d;
    }

    public static MinecraftRuntime detect() {
        try {
            return getDetector().detect();
        } catch (Throwable t) {
            return MinecraftRuntime.notDetected();
        }
    }
}
