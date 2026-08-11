package aprism.agent.runtime;

import jdk.aprismate.runtime.HeapInsight;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Default implementation of HeapInsight using JMX and Instrumentation APIs.
 *
 * @since v26.1-Alpha.6
 */
public class DefaultHeapInsight implements HeapInsight {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultHeapInsight.class.getName());
    private final java.lang.instrument.Instrumentation instrumentation;
    
    public DefaultHeapInsight(java.lang.instrument.Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }
    
    @Override
    public List<HeapRegion> getHeapRegions() {
        List<HeapRegion> regions = new ArrayList<>();
        
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                RegionType type = mapPoolToRegionType(pool.getName(), pool.getType().name());
                regions.add(new HeapRegionImpl(type, usage));
            }
        }
        
        return Collections.unmodifiableList(regions);
    }
    
    @Override
    public long getObjectSize(Object obj) {
        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }
        
        if (instrumentation == null) {
            LOGGER.warning("Instrumentation not available, returning -1");
            return -1;
        }
        
        return instrumentation.getObjectSize(obj);
    }
    
    @Override
    public long getRetainedSize(Object obj) {
        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }
        
        if (instrumentation == null) {
            return -1;
        }
        
        // Simple retained size estimation using BFS traversal
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Queue<Object> queue = new ArrayDeque<>();
        long totalSize = 0;
        
        queue.add(obj);
        visited.add(obj);
        
        // Limit traversal to prevent excessive computation
        int maxObjects = 10000;
        int count = 0;
        
        while (!queue.isEmpty() && count < maxObjects) {
            Object current = queue.poll();
            totalSize += instrumentation.getObjectSize(current);
            count++;
            
            // Get referenced objects (simplified - real implementation would use deeper reflection)
            Class<?> clazz = current.getClass();
            
            if (clazz.isArray() && !clazz.getComponentType().isPrimitive()) {
                Object[] array = (Object[]) current;
                for (Object element : array) {
                    if (element != null && visited.add(element)) {
                        queue.add(element);
                    }
                }
            }
        }
        
        return totalSize;
    }
    
    private RegionType mapPoolToRegionType(String poolName, String poolType) {
        String lower = poolName.toLowerCase();
        
        // Young generation patterns
        if (lower.contains("eden") || lower.contains("young") || lower.contains("nursery")) {
            return RegionType.YOUNG;
        }
        
        // Survivor patterns
        if (lower.contains("survivor")) {
            return RegionType.SURVIVOR;
        }
        
        // Old generation patterns
        if (lower.contains("old") || lower.contains("tenured")) {
            return RegionType.OLD;
        }
        
        // Humongous patterns (G1)
        if (lower.contains("humongous")) {
            return RegionType.HUMONGOUS;
        }
        
        // Metaspace patterns
        if (lower.contains("metaspace") || lower.contains("perm") || lower.contains("compressed class")) {
            return RegionType.METASPACE;
        }
        
        // Code cache patterns
        if (lower.contains("code")) {
            return RegionType.CODE_CACHE;
        }
        
        return RegionType.OTHER;
    }
    
    /**
     * Implementation of HeapRegion based on MemoryUsage.
     */
    private static class HeapRegionImpl implements HeapRegion {
        private final RegionType type;
        private final long size;
        private final long used;
        
        HeapRegionImpl(RegionType type, MemoryUsage usage) {
            this.type = type;
            this.size = usage.getMax() > 0 ? usage.getMax() : usage.getCommitted();
            this.used = usage.getUsed();
        }
        
        @Override
        public RegionType getType() {
            return type;
        }
        
        @Override
        public long getSize() {
            return size;
        }
        
        @Override
        public long getUsed() {
            return used;
        }
        
        @Override
        public long getFree() {
            return size - used;
        }
        
        @Override
        public String toString() {
            return String.format("HeapRegion{type=%s, size=%d, used=%d, free=%d}", 
                type, size, used, getFree());
        }
    }
}
