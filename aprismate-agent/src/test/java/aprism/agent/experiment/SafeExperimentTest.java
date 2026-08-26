package aprism.agent.experiment;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SafeExperimentTest {

    @Test
    void handleCarriesMetadata() {
        var now = Instant.now();
        var h = new ExperimentHandle("com.example.Foo", now, new byte[]{1, 2, 3});
        assertThat(h.className()).isEqualTo("com.example.Foo");
        assertThat(h.appliedAt()).isEqualTo(now);
    }

    @Test
    void successResultWrapsHandle() {
        var h = new ExperimentHandle("x.Y", Instant.now(), new byte[0]);
        var r = ExperimentResult.ok(h);
        assertThat(r.isSuccess()).isTrue();
        assertThat(((ExperimentResult.Success) r).handle().className()).isEqualTo("x.Y");
    }

    @Test
    void failureResultCarriesErrorInfo() {
        var r = ExperimentResult.fail("x.Z", "VerifyError", "bad stack map");
        assertThat(r.isSuccess()).isFalse();
        var f = (ExperimentResult.Failure) r;
        assertThat(f.className()).isEqualTo("x.Z");
        assertThat(f.errorType()).isEqualTo("VerifyError");
        assertThat(f.message()).isEqualTo("bad stack map");
    }

    @Test
    void activeExperimentsStartsEmptyWithoutInit() {
        // Without instrumentation init, tryReplace returns failure
        var r = SafeExperiment.tryReplace(String.class, new byte[]{1});
        assertThat(r.isSuccess()).isFalse();
        assertThat(SafeExperiment.activeExperiments()).isEmpty();
    }
}
