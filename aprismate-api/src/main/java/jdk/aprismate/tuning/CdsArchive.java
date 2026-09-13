package jdk.aprismate.tuning;

import java.util.List;

/**
 * Builds CDS (Class Data Sharing) archive commands for application
 * startup optimization. Use the generated commands with a ProcessBuilder
 * or the provided shell scripts to create and use AppCDS archives.
 *
 * <p>Three-step workflow:
 * <ol>
 *   <li>{@link #dumpClassListCommand} — run app once to record class list</li>
 *   <li>{@link #createArchiveCommand} — create .jsa archive from class list</li>
 *   <li>{@link #runWithArchiveCommand} — run app with the archive (faster)</li>
 * </ol>
 */
public final class CdsArchive {

    private CdsArchive() {
    }

    /**
     * Step 1: command to dump loaded class list during a trial run.
     */
    public static List<String> dumpClassListCommand(String javaHome,
            String classListFile, String classpath, String mainClass) {
        return List.of(
                PathUtils(javaHome, "bin", "java"),
                "-XX:DumpLoadedClassList=" + classListFile,
                "-cp", classpath,
                mainClass
        );
    }

    /**
     * Step 2: command to create the shared archive from the class list.
     */
    public static List<String> createArchiveCommand(String javaHome,
            String classListFile, String archiveFile, String classpath) {
        return List.of(
                PathUtils(javaHome, "bin", "java"),
                "-Xshare:dump",
                "-XX:SharedClassListFile=" + classListFile,
                "-XX:SharedArchiveFile=" + archiveFile,
                "-cp", classpath,
                "-version"
        );
    }

    /**
     * Step 3: command to run the app with the archive (faster startup).
     */
    public static List<String> runWithArchiveCommand(String javaHome,
            String archiveFile, String classpath, String mainClass) {
        return List.of(
                PathUtils(javaHome, "bin", "java"),
                "-XX:SharedArchiveFile=" + archiveFile,
                "-cp", classpath,
                mainClass
        );
    }

    /**
     * Convenience: AutoCreateSharedArchive single-step (JDK 19+).
     * The archive is created automatically on first run and used on
     * subsequent runs.
     */
    public static List<String> autoArchiveCommand(String javaHome,
            String archiveFile, String classpath, String mainClass) {
        return List.of(
                PathUtils(javaHome, "bin", "java"),
                "-XX:+AutoCreateSharedArchive",
                "-XX:SharedArchiveFile=" + archiveFile,
                "-cp", classpath,
                mainClass
        );
    }

    private static String PathUtils(String... parts) {
        return String.join(java.io.File.separator, parts);
    }
}
