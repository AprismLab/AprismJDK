package aprism.agent.optimize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Disk cache for transformed bytecode: ~/.aprismate/bytecode-cache by
 * default, keyed by sha256(className + configFingerprint + inputBytes).
 * Atomic writes (temp + move) keep concurrent readers safe; any read
 * failure degrades to a miss.
 */
public final class BytecodeCache {

    private final Path dir;

    public BytecodeCache(Path dir) {
        this.dir = dir;
    }

    public byte[] get(String key) {
        Path f = fileFor(key);
        byte[] blob;
        try {
            blob = Files.readAllBytes(f);
        } catch (IOException e) {
            return null;
        }
        if (blob.length < HASH_LEN) {
            return null;
        }
        byte[] storedHash = Arrays.copyOfRange(blob, 0, HASH_LEN);
        byte[] payload = Arrays.copyOfRange(blob, HASH_LEN, blob.length);
        if (!MessageDigest.isEqual(storedHash, sha256Of(payload))) {
            return null;
        }
        return payload;
    }

    public void put(String key, byte[] bytes) {
        try {
            Files.createDirectories(dir);
            byte[] out = new byte[HASH_LEN + bytes.length];
            System.arraycopy(sha256Of(bytes), 0, out, 0, HASH_LEN);
            System.arraycopy(bytes, 0, out, HASH_LEN, bytes.length);
            Path tmp = Files.createTempFile(dir, "t-", ".cls");
            Files.write(tmp, out);
            Files.move(tmp, fileFor(key), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            // cache is best-effort
        }
    }

    private static final int HASH_LEN = 32;

    private static byte[] sha256Of(byte[] data) {
        var md = digest();
        md.update(data);
        return md.digest();
    }

    private Path fileFor(String key) {
        return dir.resolve(sha256(key) + ".cls");
    }

    static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String cacheKey(String className, String fingerprint, byte[] input) {
        String base = className + '\0' + fingerprint + '\0' + input.length + '\0';
        var md = digest();
        md.update(base.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        md.update(input);
        return HexFormat.of().formatHex(md.digest());
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
