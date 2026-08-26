package aprism.agent.startup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StartupReportTest {

    @Test
    void emptyReport() {
        var r = new StartupReport(0, 1000, List.of(), java.util.Map.of());
        assertThat(r.totalClasses()).isZero();
        assertThat(r.windowMs()).isCloseTo(0.001, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void countsEventsAndPackages() {
        var events = List.of(
            new StartupReport.ClassLoadEvent("java/lang/String", 100, 100, 100),
            new StartupReport.ClassLoadEvent("java/util/List", 200, 200, 100),
            new StartupReport.ClassLoadEvent("com/example/App", 300, 300, 100),
            new StartupReport.ClassLoadEvent("com/example/Service", 400, 400, 100)
        );
        var pkgs = java.util.Map.of("java.lang", 1, "java.util", 1, "com.example", 2);
        var r = new StartupReport(0, 500, events, pkgs);
        assertThat(r.totalClasses()).isEqualTo(4);
        assertThat(r.packageCounts().get("com.example")).isEqualTo(2);
        assertThat(r.packageCounts().get("java.lang")).isEqualTo(1);
    }

    @Test
    void slowestLoadsSorted() {
        var events = List.of(
            new StartupReport.ClassLoadEvent("fast", 100, 100, 10),
            new StartupReport.ClassLoadEvent("slow", 2000, 2000, 1900),
            new StartupReport.ClassLoadEvent("medium", 2500, 2500, 500)
        );
        var r = new StartupReport(0, 3000, events, java.util.Map.of());
        var slowest = r.slowestLoads(2);
        assertThat(slowest.get(0).className()).isEqualTo("slow");
        assertThat(slowest.get(1).className()).isEqualTo("medium");
    }

    @Test
    void toStringContainsSummary() {
        var events = List.of(
            new StartupReport.ClassLoadEvent("java/lang/String", 100, 100, 50),
            new StartupReport.ClassLoadEvent("com/example/App", 200, 200, 100)
        );
        var pkgs = java.util.Map.of("java.lang", 1, "com.example", 1);
        var r = new StartupReport(0, 500, events, pkgs);
        String s = r.toString();
        assertThat(s).contains("2 classes");
        assertThat(s).contains("Slowest loads");
    }
}
