package jdk.aprismate.tuning;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryAdvisorTest {

    @Test
    void analyzeReturnsStructuredData() {
        Map<String, Object> a = MemoryAdvisor.analyze();
        assertThat(a).containsKeys("heap_used_mb", "heap_committed_mb",
                "non_heap_used_mb", "heap_usage_pct");
    }

    @Test
    void recommendReturnsNonEmptyList() {
        List<String> recs = MemoryAdvisor.recommend();
        // Should always recommend at least CDS + string dedup
        assertThat(recs).isNotEmpty();
        assertThat(recs).anyMatch(r -> r.contains("AutoCreateSharedArchive"));
        assertThat(recs).anyMatch(r -> r.contains("StringDeduplication"));
    }

    @Test
    void reportIsReadable() {
        String report = MemoryAdvisor.report();
        assertThat(report).contains("Memory Analysis");
        assertThat(report).contains("Heap:");
        assertThat(report).contains("Non-heap:");
    }
}
