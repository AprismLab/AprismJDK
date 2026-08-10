package aprism.agent.api.metrics;

import aprism.agent.metrics.DefaultMetricRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class MetricRegistryTest {
    private MetricRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new DefaultMetricRegistry();
    }
    
    @AfterEach
    void tearDown() {
        registry.clear();
    }
    
    @Test
    void testCounter() {
        registry.counter("test.counter", 1.0);
        registry.counter("test.counter", 2.0);
        registry.counter("test.counter", 3.0);
        
        Optional<Metric> metric = registry.getMetric("test.counter");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(6.0);
        assertThat(metric.get().getType()).isEqualTo(MetricType.COUNTER);
    }
    
    @Test
    void testCounterWithTags() {
        registry.counter("test.counter", 1.0, "env", "prod", "host", "server1");
        registry.counter("test.counter", 2.0, "env", "prod", "host", "server1");
        
        Collection<Metric> metrics = registry.getMetricsByTag("env", "prod");
        assertThat(metrics).hasSize(1);
        assertThat(metrics.iterator().next().getValue()).isEqualTo(3.0);
    }
    
    @Test
    void testCounterWithOddTags() {
        assertThatThrownBy(() -> registry.counter("test.counter", 1.0, "key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("key-value pairs");
    }
    
    @Test
    void testGauge() {
        registry.gauge("test.gauge", 42.0);
        
        Optional<Metric> metric = registry.getMetric("test.gauge");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(42.0);
        assertThat(metric.get().getType()).isEqualTo(MetricType.GAUGE);
    }
    
    @Test
    void testGaugeWithSupplier() {
        AtomicInteger counter = new AtomicInteger(0);
        registry.gauge("test.gauge", () -> (double) counter.incrementAndGet());
        
        // Each read should increment the counter
        Optional<Metric> metric1 = registry.getMetric("test.gauge");
        assertThat(metric1).isPresent();
        assertThat(metric1.get().getValue()).isGreaterThan(0.0);
        
        // Second read should show a different (larger) value
        double firstValue = metric1.get().getValue();
        Optional<Metric> metric2 = registry.getMetric("test.gauge");
        assertThat(metric2).isPresent();
        assertThat(metric2.get().getValue()).isGreaterThan(firstValue);
    }
    
    @Test
    void testGaugeWithNullSupplier() {
        assertThatThrownBy(() -> registry.gauge("test.gauge", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("valueSupplier cannot be null");
    }
    
    @Test
    void testHistogram() {
        registry.histogram("test.histogram", 10.0);
        registry.histogram("test.histogram", 20.0);
        registry.histogram("test.histogram", 30.0);
        
        Optional<Metric> metric = registry.getMetric("test.histogram");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(20.0); // mean
        assertThat(metric.get().getType()).isEqualTo(MetricType.HISTOGRAM);
    }
    
    @Test
    void testHistogramWithTags() {
        registry.histogram("test.histogram", 100.0, "operation", "read");
        registry.histogram("test.histogram", 200.0, "operation", "read");
        
        Collection<Metric> metrics = registry.getMetricsByTag("operation", "read");
        assertThat(metrics).hasSize(1);
        assertThat(metrics.iterator().next().getValue()).isEqualTo(150.0); // mean
    }
    
    @Test
    void testTimer() {
        Timer.Sample sample = registry.timer();
        assertThat(sample).isNotNull();
    }
    
    @Test
    void testTimerWithDuration() {
        registry.timer("test.timer", 1000000L); // 1ms in nanos
        
        Optional<Metric> metric = registry.getMetric("test.timer");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(1000000.0);
        assertThat(metric.get().getType()).isEqualTo(MetricType.TIMER);
    }
    
    @Test
    void testTimerWithTags() {
        registry.timer("test.timer", 5000000L, "method", "GET", "status", "200");
        
        Collection<Metric> metrics = registry.getMetricsByTag("method", "GET");
        assertThat(metrics).hasSize(1);
        assertThat(metrics.iterator().next().getValue()).isEqualTo(5000000.0);
    }
    
    @Test
    void testRecordCustomMetric() {
        Metric metric = Metric.builder()
            .name("custom.metric")
            .type(MetricType.COUNTER)
            .value(99.0)
            .tag("custom", "true")
            .build();
        
        registry.record(metric);
        
        Optional<Metric> retrieved = registry.getMetric("custom.metric");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getValue()).isEqualTo(99.0);
        assertThat(retrieved.get().getTags()).containsEntry("custom", "true");
    }
    
    @Test
    void testRecordNullMetric() {
        assertThatThrownBy(() -> registry.record(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("metric cannot be null");
    }
    
    @Test
    void testGetMetrics() {
        registry.counter("counter1", 1.0);
        registry.gauge("gauge1", 2.0);
        registry.histogram("histogram1", 3.0);
        
        Collection<Metric> metrics = registry.getMetrics();
        assertThat(metrics).hasSize(3);
    }
    
    @Test
    void testGetMetricsByTag() {
        registry.counter("metric1", 1.0, "env", "prod");
        registry.counter("metric2", 2.0, "env", "dev");
        registry.counter("metric3", 3.0, "env", "prod");
        
        Collection<Metric> prodMetrics = registry.getMetricsByTag("env", "prod");
        assertThat(prodMetrics).hasSize(2);
        
        Collection<Metric> devMetrics = registry.getMetricsByTag("env", "dev");
        assertThat(devMetrics).hasSize(1);
    }
    
    @Test
    void testGetMetricsByTagWithNullKey() {
        assertThatThrownBy(() -> registry.getMetricsByTag(null, "value"))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("tagKey cannot be null");
    }
    
    @Test
    void testGetMetricsByTagWithNullValue() {
        assertThatThrownBy(() -> registry.getMetricsByTag("key", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("tagValue cannot be null");
    }
    
    @Test
    void testRemove() {
        registry.counter("test.counter", 1.0);
        assertThat(registry.getMetric("test.counter")).isPresent();
        
        boolean removed = registry.remove("test.counter");
        assertThat(removed).isTrue();
        assertThat(registry.getMetric("test.counter")).isEmpty();
    }
    
    @Test
    void testRemoveNonExistent() {
        boolean removed = registry.remove("nonexistent");
        assertThat(removed).isFalse();
    }
    
    @Test
    void testRemoveNullName() {
        assertThatThrownBy(() -> registry.remove(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("name cannot be null");
    }
    
    @Test
    void testClear() {
        registry.counter("counter1", 1.0);
        registry.gauge("gauge1", 2.0);
        registry.histogram("histogram1", 3.0);
        
        assertThat(registry.getMetrics()).hasSize(3);
        
        registry.clear();
        
        assertThat(registry.getMetrics()).isEmpty();
    }
    
    @Test
    void testGetMetricsReturnsUnmodifiableCollection() {
        registry.counter("test.counter", 1.0);
        Collection<Metric> metrics = registry.getMetrics();
        
        assertThatThrownBy(() -> metrics.add(Metric.builder()
                .name("new.metric")
                .type(MetricType.COUNTER)
                .value(1.0)
                .build()))
            .isInstanceOf(UnsupportedOperationException.class);
    }
    
    @Test
    void testGetMetricsByTagReturnsUnmodifiableCollection() {
        registry.counter("test.counter", 1.0, "env", "prod");
        Collection<Metric> metrics = registry.getMetricsByTag("env", "prod");
        
        assertThatThrownBy(() -> metrics.add(Metric.builder()
                .name("new.metric")
                .type(MetricType.COUNTER)
                .value(1.0)
                .build()))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
