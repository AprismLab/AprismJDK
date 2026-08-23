package aprism.agent.api.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link Metric}.
 */
class MetricImpl implements Metric {
    private final String name;
    private final MetricType type;
    private final double value;
    private final Map<String, String> tags;
    private final long timestamp;
    private final String description;
    
    private MetricImpl(BuilderImpl builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.value = builder.value;
        this.tags = Collections.unmodifiableMap(new HashMap<>(builder.tags));
        this.timestamp = builder.timestamp;
        this.description = builder.description;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public MetricType getType() {
        return type;
    }
    
    @Override
    public double getValue() {
        return value;
    }
    
    @Override
    public Map<String, String> getTags() {
        return tags;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return "Metric{name='" + name + "', type=" + type + ", value=" + value + 
               ", tags=" + tags + ", timestamp=" + timestamp + "}";
    }
    
    static class BuilderImpl implements Builder {
        private String name;
        private MetricType type;
        private double value;
        private final Map<String, String> tags = new HashMap<>();
        private long timestamp = System.currentTimeMillis();
        private String description = "";
        
        @Override
        public Builder name(String name) {
            Objects.requireNonNull(name, "name cannot be null");
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("name cannot be empty");
            }
            this.name = name;
            return this;
        }
        
        @Override
        public Builder type(MetricType type) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            return this;
        }
        
        @Override
        public Builder value(double value) {
            this.value = value;
            return this;
        }
        
        @Override
        public Builder tag(String key, String value) {
            Objects.requireNonNull(key, "tag key cannot be null");
            Objects.requireNonNull(value, "tag value cannot be null");
            this.tags.put(key, value);
            return this;
        }
        
        @Override
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        @Override
        public Builder description(String description) {
            this.description = description != null ? description : "";
            return this;
        }
        
        @Override
        public Metric build() {
            if (name == null) {
                throw new IllegalStateException("name is required");
            }
            if (type == null) {
                throw new IllegalStateException("type is required");
            }
            return new MetricImpl(this);
        }
    }
}
