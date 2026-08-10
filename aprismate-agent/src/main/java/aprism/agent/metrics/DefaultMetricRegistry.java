package aprism.agent.metrics;

import aprism.agent.api.metrics.Metric;
import aprism.agent.api.metrics.MetricRegistry;
import aprism.agent.api.metrics.MetricType;
import aprism.agent.api.metrics.Timer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Default implementation of {@link MetricRegistry}.
 */
public class DefaultMetricRegistry implements MetricRegistry {
    private final Map<String, Metric> metrics = new ConcurrentHashMap<>();
    private final Map<String, Supplier<Double>> gaugeSuppliers = new ConcurrentHashMap<>();
    private final Map<String, CounterState> counters = new ConcurrentHashMap<>();
    private final Map<String, HistogramState> histograms = new ConcurrentHashMap<>();
    
    @Override
    public void counter(String name, double value) {
        Objects.requireNonNull(name, "name cannot be null");
        counters.computeIfAbsent(name, k -> new CounterState()).add(value);
        updateMetric(name, MetricType.COUNTER, () -> counters.get(name).getValue());
    }
    
    @Override
    public void counter(String name, double value, String... tags) {
        Objects.requireNonNull(name, "name cannot be null");
        validateTags(tags);
        String key = metricKey(name, tags);
        counters.computeIfAbsent(key, k -> new CounterState()).add(value);
        updateMetric(key, MetricType.COUNTER, () -> counters.get(key).getValue(), tags);
    }
    
    @Override
    public void gauge(String name, double value) {
        Objects.requireNonNull(name, "name cannot be null");
        updateMetric(name, MetricType.GAUGE, () -> value);
    }
    
    @Override
    public void gauge(String name, double value, String... tags) {
        Objects.requireNonNull(name, "name cannot be null");
        validateTags(tags);
        String key = metricKey(name, tags);
        updateMetric(key, MetricType.GAUGE, () -> value, tags);
    }
    
    @Override
    public void gauge(String name, Supplier<Double> valueSupplier) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(valueSupplier, "valueSupplier cannot be null");
        gaugeSuppliers.put(name, valueSupplier);
        updateMetric(name, MetricType.GAUGE, valueSupplier);
    }
    
    @Override
    public void histogram(String name, double value) {
        Objects.requireNonNull(name, "name cannot be null");
        histograms.computeIfAbsent(name, k -> new HistogramState()).record(value);
        updateMetric(name, MetricType.HISTOGRAM, () -> histograms.get(name).getMean());
    }
    
    @Override
    public void histogram(String name, double value, String... tags) {
        Objects.requireNonNull(name, "name cannot be null");
        validateTags(tags);
        String key = metricKey(name, tags);
        histograms.computeIfAbsent(key, k -> new HistogramState()).record(value);
        updateMetric(key, MetricType.HISTOGRAM, () -> histograms.get(key).getMean(), tags);
    }
    
    @Override
    public Timer.Sample timer() {
        return new Timer.Sample(System.nanoTime());
    }
    
    @Override
    public void timer(String name, long nanos) {
        Objects.requireNonNull(name, "name cannot be null");
        updateMetric(name, MetricType.TIMER, () -> (double) nanos);
    }
    
    @Override
    public void timer(String name, long nanos, String... tags) {
        Objects.requireNonNull(name, "name cannot be null");
        validateTags(tags);
        String key = metricKey(name, tags);
        updateMetric(key, MetricType.TIMER, () -> (double) nanos, tags);
    }
    
    @Override
    public void record(Metric metric) {
        Objects.requireNonNull(metric, "metric cannot be null");
        metrics.put(metric.getName(), metric);
    }
    
    @Override
    public Optional<Metric> getMetric(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        // Update gauge from supplier if present
        Supplier<Double> supplier = gaugeSuppliers.get(name);
        if (supplier != null) {
            updateMetric(name, MetricType.GAUGE, supplier);
        }
        return Optional.ofNullable(metrics.get(name));
    }
    
    @Override
    public Collection<Metric> getMetrics() {
        // Update all gauges from suppliers
        gaugeSuppliers.forEach((name, supplier) -> 
            updateMetric(name, MetricType.GAUGE, supplier));
        return Collections.unmodifiableCollection(new ArrayList<>(metrics.values()));
    }
    
    @Override
    public Collection<Metric> getMetricsByTag(String tagKey, String tagValue) {
        Objects.requireNonNull(tagKey, "tagKey cannot be null");
        Objects.requireNonNull(tagValue, "tagValue cannot be null");
        List<Metric> result = new ArrayList<>();
        for (Metric metric : getMetrics()) {
            String value = metric.getTags().get(tagKey);
            if (tagValue.equals(value)) {
                result.add(metric);
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    @Override
    public void clear() {
        metrics.clear();
        gaugeSuppliers.clear();
        counters.clear();
        histograms.clear();
    }
    
    @Override
    public boolean remove(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        gaugeSuppliers.remove(name);
        counters.remove(name);
        histograms.remove(name);
        return metrics.remove(name) != null;
    }
    
    private void updateMetric(String name, MetricType type, Supplier<Double> valueSupplier, String... tags) {
        Metric.Builder builder = Metric.builder()
            .name(name)
            .type(type)
            .value(valueSupplier.get());
        
        for (int i = 0; i < tags.length; i += 2) {
            builder.tag(tags[i], tags[i + 1]);
        }
        
        metrics.put(name, builder.build());
    }
    
    private String metricKey(String name, String... tags) {
        if (tags.length == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        for (int i = 0; i < tags.length; i += 2) {
            sb.append('[').append(tags[i]).append('=').append(tags[i + 1]).append(']');
        }
        return sb.toString();
    }
    
    private void validateTags(String... tags) {
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("tags must be key-value pairs (even number of arguments)");
        }
    }
    
    private static class CounterState {
        private double value = 0.0;
        
        synchronized void add(double delta) {
            value += delta;
        }
        
        synchronized double getValue() {
            return value;
        }
    }
    
    private static class HistogramState {
        private long count = 0;
        private double sum = 0.0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        
        synchronized void record(double value) {
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        
        synchronized double getMean() {
            return count > 0 ? sum / count : 0.0;
        }
        
        synchronized long getCount() {
            return count;
        }
        
        synchronized double getMin() {
            return min;
        }
        
        synchronized double getMax() {
            return max;
        }
    }
}
