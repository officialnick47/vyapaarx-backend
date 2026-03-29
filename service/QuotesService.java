package controller;

import com.sun.net.httpserver.HttpExchange;
import service.QuotesService;

import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class QuotesController {

    public static void handle(HttpExchange exchange) {
        try {

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);

            String symbols = params.get("symbols");

            String response = QuotesService.getQuotes(symbols);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();

        if (query == null) return result;

        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }

        return result;
    }
}
