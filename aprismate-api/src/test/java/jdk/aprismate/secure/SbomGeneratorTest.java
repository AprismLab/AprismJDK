package jdk.aprismate.secure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SbomGeneratorTest {

    @TempDir
    Path dir;

    @Test
    void generatesValidCycloneDxJson() throws Exception {
        Path file = dir.resolve("test.jar");
        Files.write(file, new byte[]{1, 2, 3, 4, 5});
        Map<String, Path> artifacts = Map.of("test.jar", file);

        String sbom = SbomGenerator.generate(artifacts, "v26.4-Alpha.6");

        assertThat(sbom).contains("\"bomFormat\":\"CycloneDX\"");
        assertThat(sbom).contains("\"specVersion\":\"1.5\"");
        assertThat(sbom).contains("\"name\":\"AprismJDK\"");
        assertThat(sbom).contains("\"hashes\"");
        assertThat(sbom).contains("SHA-256");
    }

    @Test
    void hashIsDeterministic() throws Exception {
        Path f1 = dir.resolve("a.jar");
        Path f2 = dir.resolve("b.jar");
        byte[] data = new byte[]{0x01, 0x7F, (byte) 0x80, 0x42};
        Files.write(f1, data);
        Files.write(f2, data);
        String h1 = SbomGenerator.sha256(f1);
        String h2 = SbomGenerator.sha256(f2);
        assertThat(h1).isEqualTo(h2);
        assertThat(h1).hasSize(64); // SHA-256 hex
    }

    @Test
    void multipleArtifactsSorted() throws Exception {
        Path a = dir.resolve("aaa.jar"); Files.write(a, new byte[]{1});
        Path z = dir.resolve("zzz.jar"); Files.write(z, new byte[]{2});
        Map<String, Path> artifacts = new LinkedHashMap<>();
        artifacts.put("zzz.jar", z);
        artifacts.put("aaa.jar", a);
        String sbom = SbomGenerator.generate(artifacts, "v26.3");
        int zPos = sbom.indexOf("zzz.jar");
        int aPos = sbom.indexOf("aaa.jar");
        assertThat(aPos).isLessThan(zPos); // TreeMap sorts alphabetically
    }

    @Test
    void missingFileSkipped() {
        Map<String, Path> artifacts = Map.of(
                "real.jar", dir.resolve("real.jar"),       // doesn't exist
                "another.jar", dir.resolve("another.jar")  // doesn't exist
        );
        String sbom = SbomGenerator.generate(artifacts, "v26.3");
        assertThat(sbom).doesNotContain("real.jar"); // skipped
        // components array should be empty
        assertThat(sbom).contains("\"components\":[");
    }
}
