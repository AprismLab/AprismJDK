package jdk.aprismate.tuning;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StartupBenchmarkTest {

    @Test
    void classExists() {
        // The benchmark is designed to run as a main class; verify it loads
        assertThat(StartupBenchmark.class).isNotNull();
        assertThat(StartupBenchmark.class.getDeclaredMethods().length).isPositive();
    }
}
