package aprism.agent.optimize;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parsed optimization rule set (simple line format):
 *
 * <pre>
 *   # fully-elide void calls whose owner+name match
 *   elide = com.example.Debug log
 *   # inject ProbeSink.methodEnter("Cls.m") at entry of matches
 *   probe-enter = com.example.Hot compute
 *   # override cache location (default ~/.aprismate/bytecode-cache)
 *   cache-dir = /some/dir
 * </pre>
 */
public final class OptimizerConfig {

    /** owner-class internal name -> set of method names to elide */
    private final Set<String> elisions;
    private final Set<String> probes;
    private final Path cacheDir;

    OptimizerConfig(Set<String> elisions, Set<String> probes, Path cacheDir) {
        this.elisions = Collections.unmodifiableSet(elisions);
        this.probes = Collections.unmodifiableSet(probes);
        this.cacheDir = cacheDir;
    }

    public static OptimizerConfig empty() {
        return new OptimizerConfig(Set.of(), Set.of(), defaultCacheDir());
    }

    public Set<String> elisions() {
        return elisions;
    }

    public Set<String> probes() {
        return probes;
    }

    public Path cacheDir() {
        return cacheDir;
    }

    public boolean isEmpty() {
        return elisions.isEmpty() && probes.isEmpty();
    }

    /**
     * Key for a rule pair ("owner" + '.' + methodName).
     */
    public static String key(String ownerInternal, String methodName) {
        return ownerInternal + "." + methodName;
    }

    public static Path defaultCacheDir() {
        String home = System.getProperty("user.home", ".");
        return Path.of(home, ".aprismate", "bytecode-cache");
    }

    public static OptimizerConfig parse(Path rulesFile) throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(rulesFile)) {
            p.load(in);
        }
        Set<String> elisions = new HashSet<>();
        Set<String> probes = new HashSet<>();
        Path cache = defaultCacheDir();

        for (var e : p.entrySet()) {
            String k = String.valueOf(e.getKey()).trim();
            String v = String.valueOf(e.getValue()).trim();
            switch (k) {
                case "elide" -> elisions.addAll(tokens(v));
                case "probe-enter" -> probes.addAll(tokens(v));
                case "cache-dir" -> cache = Path.of(v);
                default -> { /* unknown keys ignored (forward compat) */ }
            }
        }
        return new OptimizerConfig(elisions, probes, cache);
    }

    /**
     * Value is "owner.com name [name2 ...]" -> expands to owner.name keys.
     */
    private static Set<String> tokens(String value) {
        if (value.isBlank()) {
            return Set.of();
        }
        String[] parts = value.split("\\s+");
        if (parts.length < 2) {
            // single token form "fully.qualified.Owner#method"
            int hash = parts[0].indexOf('#');
            if (hash > 0) {
                return Set.of(parts[0].replace('#', '.'));
            }
            return Set.of();
        }
        String owner = parts[0];
        var out = new HashSet<String>();
        for (int i = 1; i < parts.length; i++) {
            out.add(owner + "." + parts[i]);
        }
        return out;
    }

    /**
     * Stable fingerprint of the semantic rules; feeds the cache key so a
     * rule change invalidates cached entries naturally.
     */
    public String fingerprint() {
        String joined = elisions.stream().sorted().collect(Collectors.joining(","))
                + "|" + probes.stream().sorted().collect(Collectors.joining(","));
        return Integer.toHexString(joined.hashCode());
    }
}
