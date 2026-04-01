import com.sun.net.httpserver.HttpServer;
import controller.InstrumentsController;
import controller.MarketController;
import controller.QuotesController;
import controller.TradeController;
import controller.UserController;
import service.InstrumentsService;
import service.QuotesService;
import service.SearchService;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        // Bootstrap defaults first
        InstrumentsService.bootstrapDefaults();

        // Background instrument master sync
        SearchService.initMasterData();

        // Optional sync hook for future external/master merge
        InstrumentsService.syncMasterIfConfigured();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // Warm only important quotes at a safer interval
        scheduler.scheduleAtFixedRate(() -> {
            try {
                QuotesService.warmCriticalQuotes();
            } catch (Exception e) {
                System.out.println("Quote warmup failed: " + e.getMessage());
            }
        }, 3, 8, TimeUnit.SECONDS);

        // Periodic instrument refresh
        scheduler.scheduleAtFixedRate(() -> {
            try {
                SearchService.refreshMasterDataIfEmpty();
                InstrumentsService.syncMasterIfConfigured();
            } catch (Exception e) {
                System.out.println("Instrument refresh hook failed: " + e.getMessage());
            }
        }, 10, 360, TimeUnit.MINUTES);

        // Routes
        server.createContext("/quotes", QuotesController::handle);
        server.createContext("/quotes/batch", QuotesController::handle);
        server.createContext("/market/status", MarketController::handle);
        server.createContext("/user/me", UserController::handle);
        server.createContext("/trade", TradeController::handle);

        server.createContext("/instruments/search", InstrumentsController::handle);
        server.createContext("/instruments/indices", InstrumentsController::handle);
        server.createContext("/instruments/sync", InstrumentsController::handle);

        server.createContext("/health", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                String response = "{"
                        + "\"success\":true,"
                        + "\"status\":\"ok\","
                        + "\"service\":\"vyapaarx-backend\","
                        + "\"timestamp\":" + System.currentTimeMillis() + ","
                        + "\"instruments\":" + InstrumentsService.getHealthStatsJson()
                        + "}";

                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.getResponseHeaders().set("Cache-Control", "no-store");

                if ("HEAD".equalsIgnoreCase(method)) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                }
            } catch (Exception e) {
                String error = "{\"success\":false,\"status\":\"error\",\"message\":\"" + escape(e.getMessage()) + "\"}";
                byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });

        // Safer for small/free infra
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("VyapaarX Backend Live on Port: " + port);
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
