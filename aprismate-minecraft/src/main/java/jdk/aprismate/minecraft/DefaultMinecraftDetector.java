package jdk.aprismate.minecraft;

import java.util.Locale;
import java.util.Optional;

/**
 * Heuristic detector driven by three evidence sources, each injectable
 * for tests: the JVM command line, class presence probes against the
 * context loader, and main-class naming.
 */
public final class DefaultMinecraftDetector implements MinecraftDetector {

    /** Probes a class name on the current class loader hierarchy. */
    @FunctionalInterface
    public interface ClassProbe {
        boolean present(String className);
    }

    private final ClassProbe probe;
    private final java.util.function.Supplier<String> commandSupplier;

    public DefaultMinecraftDetector() {
        this(DefaultMinecraftDetector::defaultProbe,
             () -> System.getProperty("sun.java.command", ""));
    }

    DefaultMinecraftDetector(ClassProbe probe, java.util.function.Supplier<String> commandSupplier) {
        this.probe = probe;
        this.commandSupplier = commandSupplier;
    }

    private static boolean defaultProbe(String cn) {
        try {
            Class.forName(cn, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public MinecraftRuntime detect() {
        String command = commandSupplier.get();
        if (command == null) {
            command = "";
        }
        String lower = command.toLowerCase(Locale.ROOT);

        boolean mcMain = lower.contains("net.minecraft.client.main.main")
                || lower.contains("net.minecraft.server.minecraftserver")
                || lower.contains("net.fabricmc.devlaunchinjector")
                || lower.contains("cpw.mods.bootstraplauncher")
                || lower.contains("org.quiltmc.loader.impl.launcher");
        boolean classesPresent = any(
                "net.minecraft.world.level.Level",
                "net.minecraft.server.MinecraftServer",
                "ave"); // legacy 1.8-era obfuscated client main

        if (!mcMain && !classesPresent) {
            return MinecraftRuntime.notDetected();
        }

        ModLoaderType loader = detectLoader(lower);
        Side side = detectSide(lower);
        Optional<String> version = extractVersion(command);

        return new MinecraftRuntime(true, loader, side, version,
                Optional.ofNullable(mainClassOf(command)), Optional.of(command));
    }

    private boolean any(String... classNames) {
        for (String cn : classNames) {
            if (probe.present(cn)) {
                return true;
            }
        }
        return false;
    }

    private ModLoaderType detectLoader(String lowerCommand) {
        if (probe.present("net.neoforged.fml.loading.FMLLoader")
                || lowerCommand.contains("neoforge")) {
            return ModLoaderType.NEOFORGE;
        }
        if (probe.present("cpw.mods.modlauncher.Launcher")
                || lowerCommand.contains("forge")) {
            return ModLoaderType.FORGE;
        }
        if (probe.present("org.quiltmc.loader.impl.QuiltLoaderImpl")) {
            return ModLoaderType.QUILT;
        }
        if (probe.present("net.fabricmc.loader.impl.FabricLoaderImpl")) {
            return ModLoaderType.FABRIC;
        }
        if (probe.present("aprism.agent.AprismBootstrap")
                || jdk.aprismate.VmInfo.isAprismJdk()) {
            return ModLoaderType.APRISM;
        }
        if (lowerCommand.contains("optifine")) {
            return ModLoaderType.OPTIFINE;
        }
        return ModLoaderType.VANILLA;
    }

    private Side detectSide(String lowerCommand) {
        if (probe.present("net.minecraft.server.dedicated.DedicatedServer")) {
            return Side.DEDICATED_SERVER;
        }
        if (lowerCommand.contains("nogui") || lowerCommand.contains("--port")) {
            return Side.DEDICATED_SERVER;
        }
        if (lowerCommand.contains("net.minecraft.client.main.main")) {
            return Side.CLIENT;
        }
        return Side.UNKNOWN;
    }

    private Optional<String> extractVersion(String command) {
        String fromProp = System.getProperty("minecraft.version");
        if (notBlank(fromProp)) {
            return Optional.of(fromProp.trim());
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:--fml\\.mcVersion|--version|minecraftVersion=)[= ]?([0-9][0-9.a-zA-Z_\\-]*)")
                .matcher(command);
        if (m.find()) {
            return Optional.of(m.group(1));
        }
        m = java.util.regex.Pattern.compile("\\b(1\\.[0-9]{1,2}(\\.[0-9]+)?)[ \\t]")
                .matcher(command + " ");
        if (m.find()) {
            return Optional.of(m.group(1));
        }
        return Optional.empty();
    }

    private String mainClassOf(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length > 0 && parts[0].contains(".") && !parts[0].startsWith("-")) {
            return parts[0];
        }
        return null;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
