package jdk.aprismate.tuning;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PerfRegressionTest {

    @Test
    void noBaselineMeansSkipped() {
        var pr = new PerfRegression();
        var results = pr.measure("unknown", (Runnable) () -> {});
        assertThat(results.get("unknown").regressed()).isFalse();
        assertThat(results.get("unknown").detail()).isEqualTo("no baseline");
    }

    @Test
    void fastOperationNotFlagged() {
        var pr = new PerfRegression().threshold(2.0);
        pr.baseline("op", 1_000_000L); // 1ms baseline (generous)
        var results = pr.measure("op", (Runnable) () -> { var x = 42; });
        // Should NOT regress (we do 100K empty ops in << 1ms)
        assertThat(results.get("op").regressed()).isFalse();
    }

    @Test
    void artificiallySlowOperationFlagged() {
        var pr = new PerfRegression().threshold(1.05); // strict 5% tolerance
        pr.baseline("slow_op", 1L); // 1ns baseline (impossibly fast)

        Runnable deliberatelySlow = () -> {
            try { Thread.sleep(0, 500); } catch (InterruptedException ignored) {}
        };
        var results = pr.measure("slow_op", deliberatelySlow);
        // 100K iterations of 500ns sleep = way over 1ns baseline
        assertThat(results.get("slow_op").regressed()).isTrue();
    }

    @Test
    void thresholdRespected() {
        var pr = new PerfRegression().threshold(100.0); // very lenient
        pr.baseline("op", 1L);
        Runnable someWork = () -> { Math.sqrt(42); };
        var results = pr.measure("op", someWork);
        // 100x tolerance means almost nothing regresses
        assertThat(results.get("op").regressed()).isFalse();
    }

    @Test
    void multipleOperationsTracked() {
        var pr = new PerfRegression().threshold(2.0);
        pr.baseline("fast", 1_000_000L);
        pr.baseline("slow", 1L);

        Runnable fast = () -> { Math.sqrt(42); };
        Runnable slow = () -> { Thread.yield(); };

        Map<String, PerfRegression.RegressionResult> results =
                pr.measure("fast", fast, "slow", slow);

        assertThat(results).hasSize(2);
        assertThat(results.get("fast")).isNotNull();
        assertThat(results.get("slow")).isNotNull();
    }
}
