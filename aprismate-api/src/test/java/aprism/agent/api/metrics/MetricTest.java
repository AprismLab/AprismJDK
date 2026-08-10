package aprism.agent.api.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MetricTest {
    
    @Test
    void testMetricBuilder() {
        Metric metric = Metric.builder()
            .name("test.metric")
            .type(MetricType.COUNTER)
            .value(42.0)
            .tag("env", "test")
            .tag("host", "localhost")
            .description("Test metric")
            .build();
        
        assertThat(metric.getName()).isEqualTo("test.metric");
        assertThat(metric.getType()).isEqualTo(MetricType.COUNTER);
        assertThat(metric.getValue()).isEqualTo(42.0);
        assertThat(metric.getTags()).containsEntry("env", "test")
                                    .containsEntry("host", "localhost");
        assertThat(metric.getDescription()).isEqualTo("Test metric");
        assertThat(metric.getTimestamp()).isGreaterThan(0);
    }
    
    @Test
    void testMetricBuilderWithCustomTimestamp() {
        long timestamp = 1234567890L;
        Metric metric = Metric.builder()
            .name("test.metric")
            .type(MetricType.GAUGE)
            .value(100.0)
            .timestamp(timestamp)
            .build();
        
        assertThat(metric.getTimestamp()).isEqualTo(timestamp);
    }
    
    @Test
    void testMetricBuilderRequiresName() {
        assertThatThrownBy(() -> Metric.builder()
                .type(MetricType.COUNTER)
                .value(1.0)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("name is required");
    }
    
    @Test
    void testMetricBuilderRequiresType() {
        assertThatThrownBy(() -> Metric.builder()
                .name("test.metric")
                .value(1.0)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("type is required");
    }
    
    @Test
    void testMetricBuilderRejectsNullName() {
        assertThatThrownBy(() -> Metric.builder().name(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("name cannot be null");
    }
    
    @Test
    void testMetricBuilderRejectsEmptyName() {
        assertThatThrownBy(() -> Metric.builder().name("  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name cannot be empty");
    }
    
    @Test
    void testMetricBuilderRejectsNullType() {
        assertThatThrownBy(() -> Metric.builder().type(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("type cannot be null");
    }
    
    @Test
    void testMetricBuilderRejectsNullTagKey() {
        assertThatThrownBy(() -> Metric.builder()
                .name("test.metric")
                .type(MetricType.COUNTER)
                .tag(null, "value"))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("tag key cannot be null");
    }
    
    @Test
    void testMetricBuilderRejectsNullTagValue() {
        assertThatThrownBy(() -> Metric.builder()
                .name("test.metric")
                .type(MetricType.COUNTER)
                .tag("key", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("tag value cannot be null");
    }
    
    @Test
    void testMetricWithNoDescription() {
        Metric metric = Metric.builder()
            .name("test.metric")
            .type(MetricType.COUNTER)
            .value(1.0)
            .build();
        
        assertThat(metric.getDescription()).isEmpty();
    }
    
    @Test
    void testMetricWithNullDescription() {
        Metric metric = Metric.builder()
            .name("test.metric")
            .type(MetricType.COUNTER)
            .value(1.0)
            .description(null)
            .build();
        
        assertThat(metric.getDescription()).isEmpty();
    }
    
    @Test
    void testMetricToString() {
        Metric metric = Metric.builder()
            .name("test.metric")
            .type(MetricType.COUNTER)
            .value(42.0)
            .build();
        
        assertThat(metric.toString())
            .contains("test.metric")
            .contains("COUNTER")
            .contains("42.0");
    }
    
    @Test
    void testMetricTagsAreUnmodifiable() {
        Metric metric = Metric.builder()
            .name("test.metric")
            .type(MetricType.COUNTER)
            .value(1.0)
            .tag("key", "value")
            .build();
        
        assertThatThrownBy(() -> metric.getTags().put("new", "value"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
