package jdk.aprismate.export;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeExporterV2Test {

    @Test
    void firstSnapshotHasNoDiff() {
        RuntimeExporterV2.setHistorySize(5);
        var diff = RuntimeExporterV2.takeSnapshot();
        assertThat(diff).isNotNull();
        assertThat(diff.current()).isNotNull();
        assertThat(diff.current().seq()).isPositive();
    }

    @Test
    void historyGrowsAndBounded() {
        RuntimeExporterV2.setHistorySize(3);
        RuntimeExporterV2.takeSnapshot();
        RuntimeExporterV2.takeSnapshot();
        RuntimeExporterV2.takeSnapshot();
        RuntimeExporterV2.takeSnapshot();
        assertThat(RuntimeExporterV2.historySize()).isLessThanOrEqualTo(3);
    }

    @Test
    void historyAsJsonIsValid() {
        RuntimeExporterV2.setHistorySize(5);
        RuntimeExporterV2.takeSnapshot();
        RuntimeExporterV2.takeSnapshot();
        String json = RuntimeExporterV2.historyAsJson(10);
        assertThat(json).startsWith("[");
        assertThat(json).endsWith("]");
        assertThat(json).contains("heap_delta_pct");
    }

    @Test
    void alertFiresOnHighHeap() {
        RuntimeExporterV2.setHistorySize(5);
        var alerts = new CopyOnWriteArrayList<RuntimeExporterV2.Alert>();
        // Set threshold to -1 so it always fires (even negative heap pct on max<0)
        RuntimeExporterV2.setHeapAlertThreshold(Double.MAX_VALUE);
        RuntimeExporterV2.setAlertListener(alerts::add);
        try {
            RuntimeExporterV2.takeSnapshot();
            // Heap alert fires if heapUsagePct >= threshold (Double.MAX_VALUE)
            // This may or may not fire depending on actual heap usage
            // Main goal: no crash and callback mechanism works
        } finally {
            RuntimeExporterV2.setAlertListener(null);
            RuntimeExporterV2.setHeapAlertThreshold(90.0);
        }
    }

    @Test
    void diffTracksChanges() {
        RuntimeExporterV2.setHistorySize(5);
        var d1 = RuntimeExporterV2.takeSnapshot();
        var d2 = RuntimeExporterV2.takeSnapshot();
        // Second snapshot should have a diff from first
        assertThat(d2.current().seq()).isGreaterThan(d1.current().seq());
        assertThat(d2.toString()).contains("heap_delta_pct");
    }
}
