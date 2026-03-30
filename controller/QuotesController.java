package controller;

import com.sun.net.httpserver.HttpExchange;
import service.QuotesService;
import service.SearchService;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class QuotesController {

    public static void handle(HttpExchange ex) {
        try {
            // 1. SECURITY CHECK (Bearer Token)
            String auth = ex.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.equals("Bearer VyapaarX_Alpha_2026")) {
                send(ex, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            String path = ex.getRequestURI().getPath();
            String query = ex.getRequestURI().getQuery();

            // 2. ROUTING: UNIVERSAL SEARCH (Dhan Style Search)
            // Endpoint: /search?q=RELIANCE
            if (path.equals("/search")) {
                if (query != null && query.contains("q=")) {
                    String searchTerm = query.split("q=")[1];
                    String decodedSearch = URLDecoder.decode(searchTerm, StandardCharsets.UTF_8);
                    
                    List<String> results = SearchService.find(decodedSearch);
                    
                    // JSON format mein convert kar rahe hain
                    StringBuilder json = new StringBuilder("{\"results\":[");
                    for (int i = 0; i < results.size(); i++) {
                        json.append("\"").append(results.get(i)).append("\"");
                        if (i < results.size() - 1) json.append(",");
                    }
                    json.append("]}");
                    
                    send(ex, 200, json.toString());
                    return;
                }
            }

            // 3. ROUTING: LIVE QUOTES & WATCHLIST ADD
            // Endpoint: /quotes?symbols=NSE_EQ|RELIANCE
            if (path.equals("/quotes")) {
                if (query != null && query.contains("symbols=")) {
                    String symbols = query.split("symbols=")[1];
                    String decodedSymbols = URLDecoder.decode(symbols, StandardCharsets.UTF_8);
                    
                    // Service ko bol rahe hain ki naye symbols add kare
                    QuotesService.addSymbols(decodedSymbols);
                }

                // Hamesha latest cache data bhejo (₹1,00,000 wallet logic ke liye)
                send(ex, 200, QuotesService.getQuotesFromCache());
                return;
            }

            // Default 404
            send(ex, 404, "{\"error\":\"Endpoint Not Found\"}");

        } catch (Exception e) {
            e.printStackTrace();
            try { send(ex, 500, "{\"error\":\"Internal Server Error\"}"); } catch (Exception ignored) {}
        }
    }

    private static void send(HttpExchange ex, int code, String body) throws java.io.IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        // CORS handle karne ke liye (Mobile app connectivity)
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(b);
        }
    }
}
