package jdk.aprismate.tuning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CdsArchiveTest {

    private static final String JAVA = "C:\\jdk\\bin\\java.exe";
    private static final String CP = "build/classes";

    @Test
    void dumpClassListCommand() {
        List<String> cmd = CdsArchive.dumpClassListCommand(
                "C:\\jdk", "app.lst", CP, "com.example.Main");
        assertThat(cmd).containsSequence(
                "C:\\jdk\\bin\\java.exe",
                "-XX:DumpLoadedClassList=app.lst");
        assertThat(cmd).contains("-cp", CP, "com.example.Main");
    }

    @Test
    void createArchiveCommand() {
        List<String> cmd = CdsArchive.createArchiveCommand(
                "C:\\jdk", "app.lst", "app.jsa", CP);
        assertThat(cmd).contains("-Xshare:dump");
        assertThat(cmd).containsSequence(
                "-XX:SharedClassListFile=app.lst",
                "-XX:SharedArchiveFile=app.jsa");
    }

    @Test
    void runWithArchiveCommand() {
        List<String> cmd = CdsArchive.runWithArchiveCommand(
                "C:\\jdk", "app.jsa", CP, "com.example.Main");
        assertThat(cmd).contains("-XX:SharedArchiveFile=app.jsa");
        assertThat(cmd).doesNotContain("-Xshare:dump");
    }

    @Test
    void autoArchiveCommand() {
        List<String> cmd = CdsArchive.autoArchiveCommand(
                "C:\\jdk", "app.jsa", CP, "com.example.Main");
        assertThat(cmd).contains("-XX:+AutoCreateSharedArchive");
        assertThat(cmd).contains("-XX:SharedArchiveFile=app.jsa");
    }

    @Test
    void linuxPathSeparator() {
        List<String> cmd = CdsArchive.dumpClassListCommand(
                "/usr/lib/jdk", "app.lst", CP, "Main");
        assertThat(cmd.get(0)).isEqualTo("/usr/lib/jdk/bin/java");
    }
}
