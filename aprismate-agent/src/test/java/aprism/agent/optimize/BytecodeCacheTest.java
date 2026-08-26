package aprism.agent.optimize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class BytecodeCacheTest {

    @TempDir
    Path dir;

    @Test
    void putGetRoundtrip() {
        BytecodeCache c = new BytecodeCache(dir);
        byte[] data = new byte[64];
        new Random(42).nextBytes(data);
        c.put("k1", data);
        assertThat(c.get("k1")).isEqualTo(data);
    }

    @Test
    void missReturnsNull() {
        assertThat(new BytecodeCache(dir).get("absent")).isNull();
    }

    @Test
    void differentKeysIsolate() {
        BytecodeCache c = new BytecodeCache(dir);
        c.put("a", new byte[]{1});
        c.put("b", new byte[]{2});
        assertThat(c.get("a")[0]).isEqualTo((byte) 1);
        assertThat(c.get("b")[0]).isEqualTo((byte) 2);
    }

    @Test
    void overwriteWins() {
        BytecodeCache c = new BytecodeCache(dir);
        c.put("k", new byte[]{1, 1});
        c.put("k", new byte[]{9});
        assertThat(c.get("k")).containsExactly(9);
    }

    @Test
    void corruptEntryDegradesToNull() throws Exception {
        BytecodeCache c = new BytecodeCache(dir);
        c.put("key", new byte[]{5, 5});
        // simulate on-disk corruption
        try (var files = Files.list(dir)) {
            files.forEach(f -> {
                try {
                    Files.write(f, new byte[]{1, 2, 3});
                } catch (Exception ignored) {
                }
            });
        }
        assertThat(c.get("key")).isNull();
    }

    @Test
    void cacheKeySensitiveToAllInputs() {
        String base = BytecodeCache.cacheKey("A", "fp", new byte[]{1});
        assertThat(BytecodeCache.cacheKey("B", "fp", new byte[]{1})).isNotEqualTo(base);
        assertThat(BytecodeCache.cacheKey("A", "fp2", new byte[]{1})).isNotEqualTo(base);
        assertThat(BytecodeCache.cacheKey("A", "fp", new byte[]{2})).isNotEqualTo(base);
        assertThat(BytecodeCache.cacheKey("A", "fp", new byte[]{1})).isEqualTo(base);
    }
}
