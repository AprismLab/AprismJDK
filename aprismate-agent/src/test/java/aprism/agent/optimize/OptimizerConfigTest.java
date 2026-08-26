package aprism.agent.optimize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizerConfigTest {

    @TempDir
    Path dir;

    @Test
    void parsesElideAndProbeRules() throws Exception {
        Path f = dir.resolve("rules.properties");
        String cachePath = dir.resolve("cache").toString().replace('\\', '/');
        Files.writeString(f, """
                elide = com.example.Debug log trace
                probe-enter = com.example.Hot compute
                cache-dir = %s
                """.formatted(cachePath));
        OptimizerConfig cfg = OptimizerConfig.parse(f);
        assertThat(cfg.elisions()).containsExactlyInAnyOrder(
                "com.example.Debug.log", "com.example.Debug.trace");
        assertThat(cfg.probes()).containsExactly("com.example.Hot.compute");
        assertThat(cfg.cacheDir()).isEqualTo(dir.resolve("cache"));
        assertThat(cfg.isEmpty()).isFalse();
    }

    @Test
    void hashPairFormAccepted() throws Exception {
        Path f = dir.resolve("r2.properties");
        Files.writeString(f, "elide = com.example.Debug#log\n");
        assertThat(OptimizerConfig.parse(f).elisions())
                .containsExactly("com.example.Debug.log");
    }

    @Test
    void emptyFileMeansEmptyConfig() throws Exception {
        Path f = dir.resolve("empty.properties");
        Files.writeString(f, "");
        assertThat(OptimizerConfig.parse(f).isEmpty()).isTrue();
    }

    @Test
    void unknownKeysIgnored() throws Exception {
        Path f = dir.resolve("r3.properties");
        Files.writeString(f, "future-option = whatever\n");
        assertThat(OptimizerConfig.parse(f).isEmpty()).isTrue();
    }

    @Test
    void fingerprintChangesWithRules() throws Exception {
        Path a = dir.resolve("a.properties"), b = dir.resolve("b.properties");
        Files.writeString(a, "elide = x.Y m\n");
        Files.writeString(b, "elide = x.Y other\n");
        var ca = OptimizerConfig.parse(a);
        var cb = OptimizerConfig.parse(b);
        assertThat(ca.fingerprint()).isNotEqualTo(cb.fingerprint());
    }
}
