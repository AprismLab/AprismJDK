package jdk.aprismate.minecraft;

/**
 * One aggregated sampling hotspot: a frame that appeared frequently near
 * the top of sampled stacks of observed game threads.
 */
public record Hotspot(String className, String methodName, long hits, double share) {
}
