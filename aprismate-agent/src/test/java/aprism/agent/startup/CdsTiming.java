package aprism.agent.startup;

/**
 * Measures JVM startup overhead: time from process start to first
 * useful output. Run this class with and without CDS to compare.
 *
 * <p>Usage: java CdsTiming.java (single-file source-launcher mode)
 */
public class CdsTiming {
    public static void main(String[] args) {
        long now = System.nanoTime();
        // Force some class loading
        var list = new java.util.ArrayList<String>(100);
        var map = new java.util.HashMap<String, Integer>(100);
        String hello = String.format("CDS timing: %d ns since VM start", now);
        list.add(hello);
        map.put("status", list.size());
        // Force ManagementFactory init (heavier)
        var rt = java.lang.management.ManagementFactory.getRuntimeMXBean();
        System.out.println("UPTIME_MS=" + rt.getUptime());
        System.out.println("PID=" + rt.getPid());
        System.out.println("VENDOR=" + System.getProperty("java.vendor"));
        System.out.println("VERSION=" + System.getProperty("java.version"));
    }
}
