package service;

import cache.MemoryCache;
import connector.UpstoxConnector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuotesService {
    private static final String KEYS = "NSE_INDEX|Nifty 50,NSE_INDEX|Nifty Bank";

    // App dashboard calls this
    public static String getQuotesFromCache() {
        String data = MemoryCache.get("live_data");
        return (data != null) ? data : "{\"status\":\"loading\",\"msg\":\"Warming up engine...\"}";
    }

    // Background Worker calls this every 1.5s
    public static void refreshQuotes() {
        try {
            String raw = UpstoxConnector.fetchLtpQuotes(KEYS);
            if (raw != null && !raw.contains("\"error\"")) {
                MemoryCache.put("live_data", cleanData(raw));
            }
        } catch (Exception e) {
            System.err.println("Refresh Failed: " + e.getMessage());
        }
    }

    private static String cleanData(String raw) {
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{[^\\}]*?\"last_price\"\\s*:\\s*([0-9.]+)[^\\}]*?\"instrument_token\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(raw);
        java.util.List<String> list = new java.util.ArrayList<>();
        
        while (m.find()) {
            String sym = m.group(3).contains("Nifty Bank") ? "BANKNIFTY" : "NIFTY";
            list.add(String.format("{\"symbol\":\"%s\",\"ltp\":%s,\"instrument\":\"%s\"}", sym, m.group(2), m.group(3)));
        }
        
        return String.format("{\"timestamp\":%d,\"data\":[%s]}", System.currentTimeMillis(), String.join(",", list));
    }
}
