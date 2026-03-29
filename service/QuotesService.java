package service;

import cache.MemoryCache;
import connector.UpstoxConnector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuotesService {

    private static final String DEFAULT_KEYS = "NSE_INDEX|Nifty 50,NSE_INDEX|Nifty Bank";

    public static String getQuotes(String keys) throws Exception {
        if (keys == null || keys.isBlank()) {
            keys = DEFAULT_KEYS;
        }

        String cacheKey = "quotes:" + keys;
        String cached = MemoryCache.get(cacheKey);

        if (cached != null) {
            return cached;
        }

        String rawResponse = UpstoxConnector.fetchLtpQuotes(keys);

        if (rawResponse.contains("\"error\"")) {
            return rawResponse;
        }

        String normalized = normalizeResponse(rawResponse);

        MemoryCache.put(cacheKey, normalized);
        return normalized;
    }

    private static String normalizeResponse(String rawJson) {
        StringBuilder result = new StringBuilder();
        result.append("{\"timestamp\":").append(System.currentTimeMillis()).append(",\"data\":[");

        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{[^\\}]*?\"last_price\"\\s*:\\s*([0-9.]+)[^\\}]*?\"instrument_token\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(rawJson);

        boolean first = true;

        while (matcher.find()) {
            String rawSymbol = matcher.group(1);
            String ltp = matcher.group(2);
            String instrument = matcher.group(3);

            String cleanSymbol = mapSymbol(rawSymbol, instrument);

            if (!first) {
                result.append(",");
            }

            result.append("{")
                  .append("\"symbol\":\"").append(escapeJson(cleanSymbol)).append("\",")
                  .append("\"ltp\":").append(ltp).append(",")
                  .append("\"instrument\":\"").append(escapeJson(instrument)).append("\"")
                  .append("}");

            first = false;
        }

        result.append("]}");
        return result.toString();
    }

    private static String mapSymbol(String rawSymbol, String instrument) {
        String text = (rawSymbol + " " + instrument).toUpperCase();

        if (text.contains("NIFTY BANK")) {
            return "BANKNIFTY";
        }
        if (text.contains("NIFTY 50")) {
            return "NIFTY";
        }
        return rawSymbol.replace("NSE_INDEX:", "").trim().toUpperCase();
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
