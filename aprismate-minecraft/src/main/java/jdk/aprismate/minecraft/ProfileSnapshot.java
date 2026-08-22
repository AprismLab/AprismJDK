package jdk.aprismate.minecraft;

import java.time.Duration;
import java.util.List;

/**
 * Immutable result of one profiling window.
 */
public record ProfileSnapshot(
        Duration wallTime,
        int samples,
        int gameThreadSamples,
        List<Hotspot> hotspots) {
}
