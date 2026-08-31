package jdk.aprismate.secure;

import jdk.aprismate.export.Json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * CycloneDX 1.5 SBOM generator for AprismJDK artifacts.
 * Zero external dependencies — hand-rolled minimal JSON output.
 *
 * <p>Produces a valid CycloneDX SBOM describing the fork image:
 * the JDK itself, the jdk.aprismate module, the AprismateAgent jar,
 * and their component hashes.
 */
public final class SbomGenerator {

    private SbomGenerator() {
    }

    /**
     * Generates a CycloneDX JSON SBOM for a set of files.
     *
     * @param artifacts map of component name -> file path
     * @param version   AprismJDK version string
     * @return JSON string (CycloneDX 1.5 format)
     */
    public static String generate(Map<String, Path> artifacts, String version) {
        var json = new Json();
        json.startObject();
        json.key("bomFormat").value("CycloneDX");
        json.key("specVersion").value("1.5");
        json.key("serialNumber").value("urn:uuid:" + java.util.UUID.randomUUID());
        json.key("version").value(1);

        json.key("metadata").startObject();
        json.key("timestamp").value(java.time.Instant.now().toString());
        json.key("tools").startArray();
        json.startObject();
        json.key("vendor").value("AprismLab");
        json.key("name").value("AprismJDK");
        json.key("version").value(version);
        json.endObject();
        json.endArray();
        json.key("component").startObject();
        json.key("type").value("application");
        json.key("name").value("AprismJDK");
        json.key("version").value(version);
        json.key("purl").value("pkg:github/AprismLab/AprismJDK@" + version);
        json.endObject();
        json.endObject(); // metadata

        json.key("components").startArray();
        for (var entry : new TreeMap<>(artifacts).entrySet()) {
            Path file = entry.getValue();
            if (!Files.isRegularFile(file)) {
                continue;
            }
            json.startObject();
            json.key("type").value("file");
            json.key("name").value(entry.getKey());
            json.key("version").value(version);
            json.key("purl").value("pkg:generic/aprism/" + entry.getKey() + "@" + version);
            json.key("hashes").startArray();
            json.startObject();
            json.key("alg").value("SHA-256");
            json.key("content").value(sha256(file));
            json.endObject();
            json.endArray(); // hashes
            json.endObject();
        }
        json.endArray(); // components

        json.endObject();
        return json.toString();
    }

    static String sha256(Path file) {
        try (InputStream is = Files.newInputStream(file)) {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) > 0) {
                md.update(buf, 0, read);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
}
