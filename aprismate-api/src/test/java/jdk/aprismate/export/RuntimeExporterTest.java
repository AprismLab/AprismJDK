package jdk.aprismate.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeExporterTest {

    @Test
    void fullExportIsValidJson() {
        String json = RuntimeExporter.full();
        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
        assertThat(json).contains("\"jvm\"");
        assertThat(json).contains("\"memory\"");
        assertThat(json).contains("\"threads\"");
        assertThat(json).contains("\"classes\"");
        assertThat(json).contains("\"gc\"");
    }

    @Test
    void summaryIsCompact() {
        String s = RuntimeExporter.summary();
        assertThat(s.length()).isLessThan(4096);
        assertThat(s).contains("\"jvm\"");
        assertThat(s).contains("\"memory\"");
        assertThat(s).doesNotContain("\"threads\""); // excluded from summary
    }

    @Test
    void identityAlwaysPresent() {
        String json = RuntimeExporter.builder().build().export();
        assertThat(json).contains("\"jvm\"");
        assertThat(json).contains("version");
    }

    @Test
    void sectionsAreIndependent() {
        // Even if one section throws, others must be present
        String json = RuntimeExporter.builder()
                .includeMemory()
                .includeGc()
                .includeClasses()
                .includeBuffers()
                .includeProperties()
                .includeCapabilities()
                .build()
                .export();
        assertThat(json).contains("\"memory\"");
        assertThat(json).contains("\"gc\"");
        assertThat(json).contains("\"classes\"");
        assertThat(json).contains("\"buffer_pools\"");
        assertThat(json).contains("\"properties\"");
    }

    @Test
    void threadDetailRespected() {
        String json = RuntimeExporter.builder().maxThreads(3).build().export();
        assertThat(json).contains("\"top_by_cpu\"");
    }

    @Test
    void noThreadsWhenDisabled() {
        String json = RuntimeExporter.builder().threads(false).build().export();
        assertThat(json).doesNotContain("\"live\"");
    }

    @Test
    void sensitivePropertiesExcluded() {
        String json = RuntimeExporter.builder().includeProperties().build().export();
        assertThat(json).doesNotContain("java.class.path");
        assertThat(json).doesNotContain("sun.boot.library.path");
    }

    @Test
    void prettyPrintProducesReadableOutput() {
        String json = RuntimeExporter.builder()
                .prettyPrint(true)
                .allSections()
                .maxThreads(2)
                .build()
                .export();
        assertThat(json).contains("\n");
        assertThat(json).contains("  ");
    }
}
