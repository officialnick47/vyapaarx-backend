package controller;

import com.sun.net.httpserver.HttpExchange;
import service.InstrumentsService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class InstrumentsController {

    public static void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, error("Method not allowed"));
                return;
            }

            String path = exchange.getRequestURI().getPath();

            if (path.endsWith("/search")) {
                String q = getQueryParam(exchange.getRequestURI(), "q");
                String limitText = getQueryParam(exchange.getRequestURI(), "limit");
                int limit = parseLimit(limitText, 25);
                send(exchange, 200, InstrumentsService.search(q, limit));
                return;
            }

            if (path.endsWith("/indices")) {
                send(exchange, 200, InstrumentsService.getIndices());
                return;
            }

            if (path.endsWith("/sync")) {
                send(exchange, 200, InstrumentsService.getSyncStatus());
                return;
            }

            send(exchange, 404, error("Unknown instruments route"));

        } catch (Exception e) {
            send(exchange, 500, error(e.getMessage()));
        }
    }

    private static int parseLimit(String value, int defaultValue) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(1, Math.min(parsed, 100));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static String getQueryParam(URI uri, String key) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) return null;

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String error(String msg) {
        return "{\"success\":false,\"error\":\"" + escape(msg) + "\",\"data\":[]}";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
