import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.QuotesController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Root endpoint
        server.createContext("/", exchange -> {
            if (handleHead(exchange)) return;
            sendText(exchange, "VyapaarX Backend Running 🚀");
        });

        // Health check
        server.createContext("/health", exchange -> {
            if (handleHead(exchange)) return;
            sendJson(exchange, "{\"status\":\"ok\"}");
        });

        // Quotes API
        server.createContext("/quotes", exchange -> {
            if (handleHead(exchange)) return;
            QuotesController.handle(exchange);
        });

        server.setExecutor(null); // default thread pool
        server.start();

        System.out.println("Server started on port " + port);
    }

    // ================= HELPERS =================

    private static boolean handleHead(HttpExchange exchange) throws IOException {
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return true;
        }
        return false;
    }

    private static void sendText(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
