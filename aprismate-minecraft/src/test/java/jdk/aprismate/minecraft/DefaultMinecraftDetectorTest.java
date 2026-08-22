package jdk.aprismate.minecraft;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMinecraftDetectorTest {

    private DefaultMinecraftDetector detector(Set<String> presentClasses, String command) {
        Set<String> safe = presentClasses == null ? Set.of() : presentClasses;
        return new DefaultMinecraftDetector(safe::contains, () -> command);
    }

    @Test
    void stockJvmIsNotDetected() {
        MinecraftRuntime r = detector(Set.of(), "java -jar app.jar").detect();
        assertThat(r.detected()).isFalse();
        assertThat(r.loader()).isEqualTo(ModLoaderType.UNKNOWN);
        assertThat(r.gameVersion()).isEmpty();
    }

    @Test
    void vanillaClientDetectedByMainClass() {
        String cmd = "net.minecraft.client.main.Main --username Steve --version 1.21.4 --gameDir .";
        MinecraftRuntime r = detector(Set.of(), cmd).detect();
        assertThat(r.detected()).isTrue();
        assertThat(r.loader()).isEqualTo(ModLoaderType.VANILLA);
        assertThat(r.side()).isEqualTo(Side.CLIENT);
        assertThat(r.gameVersion()).contains("1.21.4");
        assertThat(r.mainClass()).contains("net.minecraft.client.main.Main");
    }

    @Test
    void fabricDetectedByLoaderClass() {
        Set<String> classes = Set.of("net.minecraft.world.level.Level",
                "net.fabricmc.loader.impl.FabricLoaderImpl");
        String cmd = "net.fabricmc.devlaunchinjector.Launcher --gameDir .";
        MinecraftRuntime r = detector(classes, cmd).detect();
        assertThat(r.detected()).isTrue();
        assertThat(r.loader()).isEqualTo(ModLoaderType.FABRIC);
    }

    @Test
    void neoforgeBeatsForgeWhenNeoClassesPresent() {
        Set<String> classes = Set.of("cpw.mods.modlauncher.Launcher",
                "net.neoforged.fml.loading.FMLLoader");
        String cmd = "cpw.mods.bootstraplauncher.BootstrapLauncher --fml.mcVersion 1.21.4 forge";
        MinecraftRuntime r = detector(classes, cmd).detect();
        assertThat(r.loader()).isEqualTo(ModLoaderType.NEOFORGE);
    }

    @Test
    void forgeDetectedByBootstrapAndArg() {
        Set<String> classes = Set.of("cpw.mods.modlauncher.Launcher");
        String cmd = "cpw.mods.bootstraplauncher.BootstrapLauncher --fml.mcVersion 1.21.1";
        MinecraftRuntime r = detector(classes, cmd).detect();
        assertThat(r.detected()).isTrue();
        assertThat(r.loader()).isEqualTo(ModLoaderType.FORGE);
        assertThat(r.gameVersion()).contains("1.21.1");
    }

    @Test
    void dedicatedServerByNoguiFlag() {
        String cmd = "net.minecraft.server.MinecraftServer --nogui --port 25565";
        MinecraftRuntime r = detector(Set.of(), cmd).detect();
        assertThat(r.side()).isEqualTo(Side.DEDICATED_SERVER);
    }

    @Test
    void quiltDetectedByLoaderClass() {
        Set<String> classes = Set.of("org.quiltmc.loader.impl.QuiltLoaderImpl");
        MinecraftRuntime r = detector(classes, "org.quiltmc.loader.impl.launcher.QuiltLauncher").detect();
        assertThat(r.loader()).isEqualTo(ModLoaderType.QUILT);
    }

    @Test
    void aprismDetectedViaVmInfoCapability() {
        MinecraftRuntime r = detector(
                Set.of(), "net.minecraft.client.main.Main").detect();
        assertThat(r.detected()).isTrue();
        assertThat(r.loader()).isIn(ModLoaderType.APRISM, ModLoaderType.VANILLA);
    }

    @Test
    void versionPropertyWinsOverCommandLine() {
        System.setProperty("minecraft.version", "26.2");
        try {
            MinecraftRuntime r = detector(Set.of(),
                    "net.minecraft.client.main.Main --version 1.21.4").detect();
            assertThat(r.gameVersion()).contains("26.2");
        } finally {
            System.clearProperty("minecraft.version");
        }
    }

    @Test
    void nullCommandDegradesToNotDetected() {
        DefaultMinecraftDetector d = new DefaultMinecraftDetector(n -> false, () -> null);
        assertThat(d.detect().detected()).isFalse();
    }

    @Test
    void shouldOptimizeMirrorsDetection() {
        assertThat(detector(Set.of(), "").shouldOptimize()).isFalse();
        assertThat(detector(Set.of("net.minecraft.world.level.Level"), "x")
                .shouldOptimize()).isTrue();
    }
}
