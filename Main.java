import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controller.QuotesController;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Root
        server.createContext("/", exchange -> {
            if (handleHead(exchange)) return;
            sendText(exchange, "VyapaarX Backend Running 🚀");
        });

        // Health
        server.createContext("/health", exchange -> {
            if (handleHead(exchange)) return;
            sendJson(exchange, "{\"status\":\"ok\"}");
        });

        // Quotes
        server.createContext("/quotes", exchange -> {
            if (handleHead(exchange)) return;
            QuotesController.handle(exchange);
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }

    private static boolean handleHead(HttpExchange exchange) throws IOException {
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return true;
        }
        return false;
    }

    private static void sendText(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
