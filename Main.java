import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Root
        server.createContext("/", exchange -> sendResponse(exchange, "VyapaarX Backend Running 🚀"));

        // Health API
        server.createContext("/health", exchange -> sendResponse(exchange, "OK"));

        // Price API (dummy)
        server.createContext("/price", exchange -> {
            String json = "{\"symbol\":\"NIFTY\",\"price\":22350}";
            sendJson(exchange, json);
        });

        // Option Chain (dummy)
        server.createContext("/option-chain", exchange -> {
            String json = "{\"status\":\"coming soon\"}";
            sendJson(exchange, json);
        });

        server.start();
        System.out.println("Server started on port " + port);
    }

    // Text response
    static void sendResponse(HttpExchange exchange, String response) throws Exception {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // JSON response
    static void sendJson(HttpExchange exchange, String json) throws Exception {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
