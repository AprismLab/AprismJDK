package jdk.aprismate.minecraft;

import java.time.Duration;

/**
 * Lightweight stack-sampling profiler for game threads (client render
 * thread / server tick thread). Experimental: sampling is best-effort
 * and must never perturb or crash the host; all failures degrade to an
 * empty snapshot.
 */
public interface MinecraftProfiler {

    /** Thread name fragments treated as "game threads" for sampling. */
    String[] GAME_THREAD_HINTS = {"Server thread", "Render thread", "Client thread", "Main"};

    ProfileSnapshot capture(Duration window);

    default ProfileSnapshot captureMillis(long millis) {
        return capture(Duration.ofMillis(Math.max(1, millis)));
    }
}
