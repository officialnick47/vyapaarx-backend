import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                sendText(exchange, "VyapaarX Backend Running 🚀");
            }
        });

        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                sendJson(exchange, "{\"status\":\"ok\",\"service\":\"vyapaarx-backend\"}");
            }
        });

        server.createContext("/price", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                sendJson(exchange, "{\"symbol\":\"NIFTY\",\"ltp\":22350.25,\"change\":120.40,\"changePercent\":0.54}");
            }
        });

        server.createContext("/option-chain", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String json = "{"
                        + "\"symbol\":\"NIFTY\","
                        + "\"expiry\":\"2026-04-30\","
                        + "\"atmStrike\":22350,"
                        + "\"data\":["
                        + "{\"strike\":22300,\"ceLtp\":145.5,\"peLtp\":118.2,\"ceOi\":120000,\"peOi\":98000},"
                        + "{\"strike\":22350,\"ceLtp\":120.0,\"peLtp\":130.4,\"ceOi\":150000,\"peOi\":140000},"
                        + "{\"strike\":22400,\"ceLtp\":98.3,\"peLtp\":155.1,\"ceOi\":132000,\"peOi\":160000}"
                        + "]"
                        + "}";
                sendJson(exchange, json);
            }
        });

        server.createContext("/paper-order", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String json = "{"
                        + "\"status\":\"success\","
                        + "\"message\":\"Paper order simulated\","
                        + "\"orderId\":\"PX-1001\""
                        + "}";
                sendJson(exchange, json);
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Server started on port " + port);
    }

    private static void sendText(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
