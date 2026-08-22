package jdk.aprismate.minecraft;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ThreadMXBean-based sampler. Fixed 5 ms sampling interval, top-frame
 * aggregation, hard cap of 4096 samples per window to bound cost.
 */
public final class SamplingMinecraftProfiler implements MinecraftProfiler {

    private static final int SAMPLE_CAP = 4096;
    private static final long INTERVAL_MS = 5;

    private final java.util.function.Supplier<Thread[]> threadSource;

    public SamplingMinecraftProfiler() {
        this(() -> Thread.getAllStackTraces().keySet().toArray(Thread[]::new));
    }

    SamplingMinecraftProfiler(java.util.function.Supplier<Thread[]> threadSource) {
        this.threadSource = threadSource;
    }

    public static MinecraftProfiler getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public ProfileSnapshot capture(Duration window) {
        long deadline = System.nanoTime() + window.toNanos();
        Map<String, long[]> frames = new HashMap<>();
        int samples = 0;
        int gameSamples = 0;

        try {
            while (System.nanoTime() < deadline && samples < SAMPLE_CAP) {
                for (Thread t : threadSource.get()) {
                    if (t == null || !isGameThread(t.getName())) {
                        continue;
                    }
                    StackTraceElement[] stack = t.getStackTrace();
                    if (stack == null || stack.length == 0) {
                        continue;
                    }
                    gameSamples++;
                    StackTraceElement top = meaningfulTop(stack);
                    if (top != null) {
                        frames.computeIfAbsent(top.getClassName() + "#" + top.getMethodName(),
                                k -> new long[]{1})[0]++;
                    }
                    samples++;
                }
                Thread.sleep(INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable ignored) {
            return new ProfileSnapshot(Duration.ZERO, 0, 0, List.of());
        }

        List<Hotspot> hotspots = toHotspots(frames, Math.max(1, gameSamples), 10);
        Duration elapsed = Duration.ofNanos(Math.max(0, System.nanoTime() - (deadline - window.toNanos())));
        return new ProfileSnapshot(elapsed, samples, gameSamples, hotspots);
    }

    private boolean isGameThread(String name) {
    String n = name.toLowerCase(Locale.ROOT);
        for (String hint : GAME_THREAD_HINTS) {
            if (n.contains(hint.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private StackTraceElement meaningfulTop(StackTraceElement[] stack) {
        for (int i = 0; i < Math.min(stack.length, 8); i++) {
            String cn = stack[i].getClassName();
            if (!cn.startsWith("java.") && !cn.startsWith("jdk.internal.")
                    && !cn.startsWith("jdk.aprismate.minecraft.")) {
                return stack[i];
            }
        }
        return null;
    }

    private List<Hotspot> toHotspots(Map<String, long[]> frames, int total, int limit) {
        List<Hotspot> list = new ArrayList<>();
        for (Map.Entry<String, long[]> e : frames.entrySet()) {
            String key = e.getKey();
            long hits = e.getValue()[0];
            int hash = key.indexOf('#');
            list.add(new Hotspot(key.substring(0, hash), key.substring(hash + 1),
                    hits, hits / (double) total));
        }
        list.sort(Comparator.comparingLong(Hotspot::hits).reversed());
        return List.copyOf(list.subList(0, Math.min(limit, list.size())));
    }

    private static final class Holder {
        static final MinecraftProfiler INSTANCE = new SamplingMinecraftProfiler();
    }

    static {
        ManagementFactory.getThreadMXBean();
    }
}
