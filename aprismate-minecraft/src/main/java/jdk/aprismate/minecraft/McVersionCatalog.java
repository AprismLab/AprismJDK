package jdk.aprismate.minecraft;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Data-driven catalog of known Minecraft versions and their typical
 * mod-loader availability. Replaces external research data as the
 * source of truth for "is this a version we recognize".
 *
 * <p>Families: {@code legacy} uses the historical 1.x scheme;
 * {@code yearly} uses Mojang's year-based scheme introduced after
 * 1.21.11 (e.g. 26.1, 26.2).
 */
public final class McVersionCatalog {

    public enum Family { LEGACY, YEARLY }

    /**
     * One known version: id plus the loaders commonly available for it.
     */
    public record Entry(String version, Family family, Set<ModLoaderType> loaders) {
    }

    private static final Set<ModLoaderType> LEGACY_LOADERS =
            Set.of(ModLoaderType.FABRIC, ModLoaderType.FORGE, ModLoaderType.QUILT);
    private static final Set<ModLoaderType> TRANSITION_LOADERS =
            Set.of(ModLoaderType.FABRIC, ModLoaderType.FORGE, ModLoaderType.NEOFORGE, ModLoaderType.QUILT);
    private static final Set<ModLoaderType> YEARLY_LOADERS =
            Set.of(ModLoaderType.FABRIC, ModLoaderType.NEOFORGE, ModLoaderType.APRISM);

    private static final List<Entry> KNOWN = List.of(
            new Entry("26.2", Family.YEARLY, YEARLY_LOADERS),
            new Entry("26.1", Family.YEARLY, YEARLY_LOADERS),
            new Entry("1.21.11", Family.LEGACY, TRANSITION_LOADERS),
            new Entry("1.21.10", Family.LEGACY, TRANSITION_LOADERS),
            new Entry("1.21.4", Family.LEGACY, TRANSITION_LOADERS),
            new Entry("1.21.1", Family.LEGACY, TRANSITION_LOADERS),
            new Entry("1.20.6", Family.LEGACY, TRANSITION_LOADERS),
            new Entry("1.20.4", Family.LEGACY, TRANSITION_LOADERS),
            new Entry("1.20.1", Family.LEGACY, LEGACY_LOADERS),
            new Entry("1.19.2", Family.LEGACY, LEGACY_LOADERS),
            new Entry("1.18.2", Family.LEGACY, LEGACY_LOADERS),
            new Entry("1.16.5", Family.LEGACY, LEGACY_LOADERS),
            new Entry("1.12.2", Family.LEGACY, Set.of(ModLoaderType.FORGE)),
            new Entry("1.8.9", Family.LEGACY, Set.of(ModLoaderType.FORGE, ModLoaderType.OPTIFINE))
    );

    private McVersionCatalog() {
    }

    /**
     * Exact lookup after trimming common suffixes (-pre, -rc, -snapshot).
     */
    public static Optional<Entry> lookup(String version) {
        if (version == null || version.isBlank()) {
            return Optional.empty();
        }
        String base = normalize(version);
        return KNOWN.stream().filter(e -> e.version().equals(base)).findFirst();
    }

    public static boolean isKnown(String version) {
        return lookup(version).isPresent();
    }

    /**
     * Family of a known version; UNKNOWN versions resolve by leading
     * digit heuristic ({@code 1.x} legacy, {@code YY.z} yearly).
     */
    public static Family familyOf(String version) {
        return lookup(version)
                .map(Entry::family)
                .orElseGet(() -> {
                    String b = normalize(version);
                    return b.startsWith("1.") ? Family.LEGACY : Family.YEARLY;
                });
    }

    /**
     * Strips pre/rc/snapshot suffixes and trailing whitespace.
     */
    public static String normalize(String version) {
        if (version == null) {
            return "";
        }
        String v = version.trim().toLowerCase(Locale.ROOT);
        int cut = v.indexOf('-');
        if (cut > 0) {
            v = v.substring(0, cut);
        }
        return v;
    }

    static List<Entry> knownEntries() {
        return KNOWN;
    }
}
