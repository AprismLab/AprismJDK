package aprism.agent.diag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosticServerTest {

    @Test
    void serverDoesNotStartWithoutProperty() {
        System.clearProperty("aprismate.diag.port");
        DiagnosticServer.tryStart();
        // No exception, no server — pass silently
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        // Use a random port to avoid conflicts
        int port = 25590 + (int) (Math.random() * 100);
        System.setProperty("aprismate.diag.port", String.valueOf(port));
        try {
            DiagnosticServer.tryStart();
            Thread.sleep(200); // server startup

            var conn = (HttpURLConnection) new URL(
                    "http://127.0.0.1:" + port + "/aprism/health").openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            assertThat(conn.getResponseCode()).isEqualTo(200);

            var body = new String(conn.getInputStream().readAllBytes());
            assertThat(body).contains("\"status\":\"ok\"");
        } finally {
            System.clearProperty("aprismate.diag.port");
            DiagnosticServer.stop();
        }
    }

    @Test
    void summaryEndpointReturnsJson() throws Exception {
        int port = 25690 + (int) (Math.random() * 100);
        System.setProperty("aprismate.diag.port", String.valueOf(port));
        try {
            DiagnosticServer.tryStart();
            Thread.sleep(200);

            var conn = (HttpURLConnection) new URL(
                    "http://127.0.0.1:" + port + "/aprism/summary").openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            assertThat(conn.getResponseCode()).isEqualTo(200);
            assertThat(conn.getHeaderField("Content-Type")).contains("application/json");

            var body = new String(conn.getInputStream().readAllBytes());
            assertThat(body).startsWith("{");
            assertThat(body).contains("\"jvm\"");
        } finally {
            System.clearProperty("aprismate.diag.port");
            DiagnosticServer.stop();
        }
    }
}
