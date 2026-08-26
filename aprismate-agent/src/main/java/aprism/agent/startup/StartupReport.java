package aprism.agent.startup;

import java.util.List;
import java.util.Map;

/**
 * Immutable startup profile: when classes were loaded, grouped by
 * package, with timing information.
 */
public final class StartupReport {

    private final long jvmStartNanos;
    private final long captureEndNanos;
    private final List<ClassLoadEvent> events;
    private final Map<String, Integer> packageCounts;

    StartupReport(long jvmStartNanos, long captureEndNanos,
                  List<ClassLoadEvent> events, Map<String, Integer> packageCounts) {
        this.jvmStartNanos = jvmStartNanos;
        this.captureEndNanos = captureEndNanos;
        this.events = List.copyOf(events);
        this.packageCounts = Map.copyOf(packageCounts);
    }

    /** Total classes captured during the profiling window. */
    public int totalClasses() {
        return events.size();
    }

    /** Wall-clock duration of the profiling window in milliseconds. */
    public double windowMs() {
        return (captureEndNanos - jvmStartNanos) / 1_000_000.0;
    }

    /** All class-load events, ordered by load time. */
    public List<ClassLoadEvent> events() {
        return events;
    }

    /** Classes loaded per top-level package, sorted descending. */
    public Map<String, Integer> packageCounts() {
        return packageCounts;
    }

    /** N classes that took the longest between consecutive loads. */
    public List<ClassLoadEvent> slowestLoads(int limit) {
        return events.stream()
                .filter(e -> e.nanosSincePrevious() > 0)
                .sorted((a, b) -> Long.compare(b.nanosSincePrevious(), a.nanosSincePrevious()))
                .limit(limit)
                .toList();
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Startup Profile: ").append(totalClasses()).append(" classes in ")
          .append(String.format("%.0f", windowMs())).append("ms\n");
        sb.append("Top packages:\n");
        packageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append('\n'));
        sb.append("Slowest loads:\n");
        for (var e : slowestLoads(5)) {
            sb.append("  ").append(e.className()).append(" (+")
              .append(String.format("%.1f", e.nanosSincePrevious() / 1_000_000.0))
              .append("ms)\n");
        }
        return sb.toString();
    }

    /**
     * One recorded class-load event.
     */
    public record ClassLoadEvent(
            String className,
            long timestampNanos,
            long nanosSinceJvmStart,
            long nanosSincePrevious) {
    }
}
