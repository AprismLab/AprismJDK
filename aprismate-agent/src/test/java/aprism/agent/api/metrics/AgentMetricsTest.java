package aprism.agent.api.metrics;

import aprism.agent.metrics.DefaultMetricRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link AgentMetrics}.
 */
class AgentMetricsTest {
    
    @BeforeEach
    void setUp() {
        AgentMetrics.reset();
    }
    
    @AfterEach
    void tearDown() {
        AgentMetrics.reset();
    }
    
    @Test
    void testSetAndGetRegistry() {
        MetricRegistry registry = new DefaultMetricRegistry();
        AgentMetrics.setRegistry(registry);
        
        assertThat(AgentMetrics.getRegistry()).isSameAs(registry);
    }
    
    @Test
    void testSetRegistryThrowsOnNull() {
        assertThatThrownBy(() -> AgentMetrics.setRegistry(null))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    void testSetRegistryReplacesExisting() {
        MetricRegistry registry1 = new DefaultMetricRegistry();
        MetricRegistry registry2 = new DefaultMetricRegistry();
        
        AgentMetrics.setRegistry(registry1);
        assertThat(AgentMetrics.getRegistry()).isSameAs(registry1);
        
        AgentMetrics.setRegistry(registry2);
        assertThat(AgentMetrics.getRegistry()).isSameAs(registry2);
    }
    
    @Test
    void testGetRegistryWithoutSet() {
        // Should throw IllegalStateException if not initialized
        assertThatThrownBy(() -> AgentMetrics.getRegistry())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("AgentMetrics not initialized");
    }
    
    @Test
    void testCounter() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("test.counter", 5.0);
        
        Optional<Metric> metric = AgentMetrics.getRegistry().getMetric("test.counter");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(5.0);
    }
    
    @Test
    void testCounterWithTags() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("test.counter", 3.0, "env", "prod");
        
        Collection<Metric> metrics = AgentMetrics.getRegistry().getMetricsByTag("env", "prod");
        assertThat(metrics).hasSize(1);
    }
    
    @Test
    void testGauge() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.gauge("test.gauge", 42.0);
        
        Optional<Metric> metric = AgentMetrics.getRegistry().getMetric("test.gauge");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(42.0);
    }
    
    @Test
    void testGaugeWithSupplier() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.gauge("test.gauge", () -> 99.0);
        
        Optional<Metric> metric = AgentMetrics.getRegistry().getMetric("test.gauge");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(99.0);
    }
    
    @Test
    void testHistogram() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.histogram("test.histogram", 10.0);
        AgentMetrics.histogram("test.histogram", 20.0);
        AgentMetrics.histogram("test.histogram", 30.0);
        
        Optional<Metric> metric = AgentMetrics.getRegistry().getMetric("test.histogram");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(20.0); // mean
    }
    
    @Test
    void testTimer() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.timer("test.timer", 1000000L);
        
        Optional<Metric> metric = AgentMetrics.getRegistry().getMetric("test.timer");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(1000000.0);
    }
    
    @Test
    void testTimerSample() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        Timer.Sample sample = AgentMetrics.timer();
        assertThat(sample).isNotNull();
        
        long elapsed = sample.stop();
        assertThat(elapsed).isGreaterThanOrEqualTo(0L);
    }
    
    @Test
    void testRecord() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        Metric metric = Metric.builder()
            .name("custom.metric")
            .type(MetricType.GAUGE)
            .value(88.0)
            .tag("custom", "true")
            .build();
        
        AgentMetrics.record(metric);
        
        Optional<Metric> retrieved = AgentMetrics.getRegistry().getMetric("custom.metric");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getValue()).isEqualTo(88.0);
    }
    
    @Test
    void testGetMetric() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("test.counter", 10.0);
        
        Optional<Metric> metric = AgentMetrics.getMetric("test.counter");
        assertThat(metric).isPresent();
        assertThat(metric.get().getValue()).isEqualTo(10.0);
    }
    
    @Test
    void testGetMetrics() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("metric1", 1.0);
        AgentMetrics.counter("metric2", 2.0);
        AgentMetrics.counter("metric3", 3.0);
        
        Collection<Metric> metrics = AgentMetrics.getMetrics();
        assertThat(metrics).hasSizeGreaterThanOrEqualTo(3);
    }
    
    @Test
    void testGetMetricsByTag() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("metric1", 1.0, "env", "prod");
        AgentMetrics.counter("metric2", 2.0, "env", "prod");
        AgentMetrics.counter("metric3", 3.0, "env", "dev");
        
        Collection<Metric> prodMetrics = AgentMetrics.getMetricsByTag("env", "prod");
        assertThat(prodMetrics).hasSize(2);
    }
    
    @Test
    void testRemove() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("test.counter", 1.0);
        assertThat(AgentMetrics.getMetric("test.counter")).isPresent();
        
        boolean removed = AgentMetrics.remove("test.counter");
        assertThat(removed).isTrue();
        assertThat(AgentMetrics.getMetric("test.counter")).isEmpty();
    }
    
    @Test
    void testClear() {
        AgentMetrics.setRegistry(new DefaultMetricRegistry());
        
        AgentMetrics.counter("metric1", 1.0);
        AgentMetrics.counter("metric2", 2.0);
        assertThat(AgentMetrics.getMetrics()).hasSizeGreaterThanOrEqualTo(2);
        
        AgentMetrics.clear();
        assertThat(AgentMetrics.getMetrics()).isEmpty();
    }
}
