package jdk.aprismate.minecraft;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class SamplingMinecraftProfilerTest {

    private static Thread named(String name, Runnable body) {
        Thread t = new Thread(body, name);
        t.setDaemon(true);
        return t;
    }

    @Test
    void emptyWindowYieldsEmptySnapshot() {
        SamplingMinecraftProfiler p = new SamplingMinecraftProfiler(() -> new Thread[0]);
        ProfileSnapshot s = p.captureMillis(20);
        assertThat(s.samples()).isZero();
        assertThat(s.hotspots()).isEmpty();
    }

    @Test
    void capturesHotspotFromGameThread() throws Exception {
        AtomicBoolean running = new AtomicBoolean(true);
        Thread worker = named("Server thread", () -> {
            while (running.get()) {
                busyTick();
            }
        });
        try {
            worker.start();
            SamplingMinecraftProfiler p = new SamplingMinecraftProfiler(() -> new Thread[]{worker});
            ProfileSnapshot s = p.captureMillis(120);

            assertThat(s.gameThreadSamples()).isGreaterThan(0);
            assertThat(s.samples()).isGreaterThan(0);
            List<Hotspot> hs = s.hotspots();
            if (!hs.isEmpty()) {
                assertThat(hs).allMatch(h -> h.hits() > 0 && h.share() >= 0.0);
                assertThat(hs.get(0).hits())
                        .isGreaterThanOrEqualTo(hs.get(hs.size() - 1).hits());
            }
        } finally {
            running.set(false);
            worker.join(1000);
        }
    }

    private static void busyTick() {
        for (int i = 0; i < 200; i++) {
            Math.sqrt(i * 7919.0);
        }
    }

    @Test
    void nonGameThreadsAreIgnored() throws Exception {
        AtomicBoolean running = new AtomicBoolean(true);
        Thread other = named("pool-1-thread-3", () -> {
            while (running.get()) {
                busyTick();
            }
        });
        try {
            other.start();
            SamplingMinecraftProfiler p = new SamplingMinecraftProfiler(() -> new Thread[]{other});
            ProfileSnapshot s = p.captureMillis(60);
            assertThat(s.gameThreadSamples()).isZero();
        } finally {
            running.set(false);
            other.join(1000);
        }
    }

    @Test
    void failingSourceDegradesGracefully() {
        SamplingMinecraftProfiler p = new SamplingMinecraftProfiler(() -> {
            throw new IllegalStateException("boom");
        });
        ProfileSnapshot s = p.capture(Duration.ofMillis(30));
        assertThat(s.samples()).isEqualTo(0);
    }

    @Test
    void interruptedSourceStopsCleanly() {
        Thread current = Thread.currentThread();
        SamplingMinecraftProfiler p = new SamplingMinecraftProfiler(() -> {
            current.interrupt();
            return new Thread[0];
        });
        ProfileSnapshot s = p.captureMillis(100);
        assertThat(s.samples()).isZero();
    }
}
