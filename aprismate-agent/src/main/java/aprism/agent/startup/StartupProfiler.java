package aprism.agent.startup;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Records class-load timing via a ClassFileTransformer installed at
 * premain. The transformer never modifies bytes (returns null always);
 * it only records timestamps for the startup waterfall.
 *
 * <p>Thread-safe: class loading is inherently concurrent.
 */
public final class StartupProfiler implements ClassFileTransformer {

    private static final int MAX_EVENTS = 50_000;
    private static final ConcurrentLinkedQueue<Event> EVENTS = new ConcurrentLinkedQueue<>();
    private static final AtomicLong LAST_NANOS = new AtomicLong();
    private static volatile long JVM_START_NANOS;
    private static volatile boolean recording;

    private StartupProfiler() {
    }

    /**
     * Starts recording. Call as early as possible (premain).
     */
    public static StartupProfiler install(java.lang.instrument.Instrumentation inst) {
        JVM_START_NANOS = System.nanoTime();
        LAST_NANOS.set(JVM_START_NANOS);
        recording = true;
        var profiler = new StartupProfiler();
        inst.addTransformer(profiler);
        return profiler;
    }

    /**
     * Stops recording and produces the report.
     */
    public static StartupReport stop() {
        recording = false;
        long endNanos = System.nanoTime();

        List<StartupReport.ClassLoadEvent> events = new ArrayList<>(EVENTS.size());
        var pkgCounts = new HashMap<String, Integer>();
        for (var e : EVENTS) {
            events.add(new StartupReport.ClassLoadEvent(
                    e.className(), e.nanos(), e.nanos() - JVM_START_NANOS,
                    e.nanos() - LAST_NANOS.get()));
            String pkg = packageName(e.className());
            pkgCounts.merge(pkg, 1, Integer::sum);
        }
        return new StartupReport(JVM_START_NANOS, endNanos, events, pkgCounts);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> beingDefined, ProtectionDomain pd,
                            byte[] input) {
        if (recording && className != null && EVENTS.size() < MAX_EVENTS) {
            long now = System.nanoTime();
            long prev = LAST_NANOS.getAndSet(now);
            EVENTS.add(new Event(className, now));
        }
        return null; // never modify
    }

    private record Event(String className, long nanos) { }

    private static String packageName(String internalName) {
        int lastSlash = internalName.lastIndexOf('/');
        if (lastSlash <= 0) {
            return "(default)";
        }
        // Take top 2 levels for readability
        String[] parts = internalName.substring(0, lastSlash).split("/");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return parts[0];
    }
}
