package jdk.aprismate.minecraft;

import java.util.Optional;

/**
 * Immutable detection result. Empty version fields indicate best-effort
 * extraction failed; detection itself never throws.
 */
public record MinecraftRuntime(
        boolean detected,
        ModLoaderType loader,
        Side side,
        Optional<String> gameVersion,
        Optional<String> mainClass,
        Optional<String> commandLine) {

    public static MinecraftRuntime notDetected() {
        return new MinecraftRuntime(false, ModLoaderType.UNKNOWN, Side.UNKNOWN,
                Optional.empty(), Optional.empty(), Optional.empty());
    }
}
