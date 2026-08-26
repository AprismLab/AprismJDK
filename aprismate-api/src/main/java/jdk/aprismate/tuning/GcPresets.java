package jdk.aprismate.tuning;

import java.util.Optional;

/**
 * Lookup and validation for GC profiles.
 */
public final class GcPresets {

    private GcPresets() {
    }

    /**
     * Looks up a profile by case-insensitive name.
     */
    public static Optional<GcProfile> forName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(GcProfile.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a human-readable list of all available profiles.
     */
    public static String describeAll() {
        var sb = new StringBuilder();
        for (var p : GcProfile.values()) {
            sb.append("  ").append(p.name().toLowerCase(java.util.Locale.ROOT))
              .append(" — ").append(p.description())
              .append("\n    flags: ").append(p.asLaunchArgs()).append('\n');
        }
        return sb.toString();
    }
}
