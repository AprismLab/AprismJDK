package jdk.aprismate.tuning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GcPresetsTest {

    @ParameterizedTest
    @ValueSource(strings = {"server", "SERVER", "desktop", "container", "compact"})
    void allProfilesResolvable(String name) {
        assertThat(GcPresets.forName(name)).isPresent();
    }

    @Test
    void unknownProfileReturnsEmpty() {
        assertThat(GcPresets.forName("turbo")).isEmpty();
        assertThat(GcPresets.forName("")).isEmpty();
        assertThat(GcPresets.forName(null)).isEmpty();
    }

    @Test
    void serverProfileHasG1AndParallel() {
        var p = GcPresets.forName("server").orElseThrow();
        assertThat(p.options()).anyMatch(o -> o.contains("UseG1GC"));
        assertThat(p.options()).anyMatch(o -> o.contains("ParallelRefProc"));
        assertThat(p.description()).contains("Throughput");
    }

    @Test
    void desktopProfileHasZGC() {
        var p = GcPresets.forName("desktop").orElseThrow();
        assertThat(p.options()).anyMatch(o -> o.contains("UseZGC"));
    }

    @Test
    void containerProfileHasCgroupSupport() {
        var p = GcPresets.forName("container").orElseThrow();
        assertThat(p.options()).anyMatch(o -> o.contains("ContainerSupport"));
        assertThat(p.options()).anyMatch(o -> o.contains("MaxRAMPercentage"));
    }

    @Test
    void compactProfileHasSmallHeap() {
        var p = GcPresets.forName("compact").orElseThrow();
        assertThat(p.options()).anyMatch(o -> o.startsWith("-Xmx256m"));
    }

    @Test
    void describeAllContainsEveryProfile() {
        String d = GcPresets.describeAll();
        for (var p : GcProfile.values()) {
            assertThat(d).contains(p.name().toLowerCase(java.util.Locale.ROOT));
            assertThat(d).contains(p.asLaunchArgs());
        }
    }
}
