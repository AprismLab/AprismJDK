package aprism.agent.optimize;

/**
 * Static sink for injected entry probes. Kept deliberately tiny and
 * allocation-free so woven hot paths stay cheap.
 */
public final class ProbeSink {

    private static volatile long entries;

    private ProbeSink() {
    }

    public static void methodEnter(String tag) {
        entries++;
    }

    public static long totalEntries() {
        return entries;
    }
}
