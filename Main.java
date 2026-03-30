// Main.java (Updated for Render Background Worker)
import com.sun.net.httpserver.HttpServer;
import controller.QuotesController;
import service.QuotesService;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Render dynamically assigns a port
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // --- BACKGROUND WORKER: This keeps data fresh every 1.5 seconds ---
        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        worker.scheduleAtFixedRate(QuotesService::refreshQuotes, 0, 1500, TimeUnit.MILLISECONDS);

        // API Endpoints
        server.createContext("/quotes", QuotesController::handle);
        server.createContext("/health", ex -> {
            byte[] r = "{\"status\":\"ok\"}".getBytes();
            ex.sendResponseHeaders(200, r.length);
            ex.getResponseBody().write(r);
            ex.getResponseBody().close();
        });

        server.setExecutor(Executors.newFixedThreadPool(5)); // Stable thread pool
        server.start();
        System.out.println("VyapaarX Backend Live on Port: " + port);
    }
}
