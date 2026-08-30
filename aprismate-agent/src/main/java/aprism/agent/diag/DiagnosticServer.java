package aprism.agent.diag;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Lightweight HTTP diagnostic endpoint. Binds to loopback only.
 * Opt-in via -Daprismate.diag.port=<port> (default 25590).
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /aprism/state — full runtime JSON (RuntimeExporter.full())</li>
 *   <li>GET /aprism/summary — compact summary (RuntimeExporter.summary())</li>
 *   <li>GET /aprism/experiments — active SafeExperiment list</li>
 *   <li>GET /aprism/health — liveness check</li>
 * </ul>
 */
public final class DiagnosticServer {

    private static final int DEFAULT_PORT = 25590;
    private static volatile HttpServer server;

    private DiagnosticServer() {
    }

    /**
     * Starts the diagnostic server if enabled. Fail-safe: any error
     * logs and continues without the server.
     */
    public static void tryStart() {
        String portStr = System.getProperty("aprismate.diag.port");
        if (portStr == null) {
            return; // opt-in only
        }
        try {
            int port = Integer.parseInt(portStr);
            var addr = new InetSocketAddress("127.0.0.1", port);
            var srv = HttpServer.create(addr, 4);
            srv.setExecutor(Executors.newFixedThreadPool(2, r -> {
                var t = new Thread(r, "aprism-diag");
                t.setDaemon(true);
                return t;
            }));

            srv.createContext("/aprism/state", exchange -> {
                byte[] body = jdk.aprismate.export.RuntimeExporter.full()
                        .getBytes(StandardCharsets.UTF_8);
                respond(exchange, 200, body, "application/json");
            });

            srv.createContext("/aprism/summary", exchange -> {
                byte[] body = jdk.aprismate.export.RuntimeExporter.summary()
                        .getBytes(StandardCharsets.UTF_8);
                respond(exchange, 200, body, "application/json");
            });

            srv.createContext("/aprism/experiments", exchange -> {
                var sb = new StringBuilder("[");
                var experiments = aprism.agent.experiment.SafeExperiment.activeExperiments();
                for (int i = 0; i < experiments.size(); i++) {
                    if (i > 0) sb.append(",");
                    var e = experiments.get(i);
                    sb.append("{\"className\":\"").append(e.className())
                      .append("\",\"appliedAt\":\"").append(e.appliedAt()).append("\"}");
                }
                sb.append("]");
                respond(exchange, 200, sb.toString().getBytes(StandardCharsets.UTF_8), "application/json");
            });

            srv.createContext("/aprism/health", exchange -> {
                respond(exchange, 200, "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8),
                        "application/json");
            });

            srv.start();
            server = srv;
            System.out.println("[AprismateAgent] diagnostic server on http://127.0.0.1:" + port + "/aprism/");
        } catch (Throwable t) {
            System.err.println("[AprismateAgent] FAIL-SAFE: diagnostic server failed, continuing without it");
            System.err.println("  " + t.getMessage());
        }
    }

    public static void stop() {
        var s = server;
        if (s != null) {
            s.stop(0);
            server = null;
        }
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange,
                                int status, byte[] body, String contentType) {
        try {
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        } catch (Throwable ignored) {
        }
    }
}
