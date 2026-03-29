package service;

import cache.MemoryCache;
import connector.UpstoxConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuotesService {

    private static final String DEFAULT_KEYS = "NSE_INDEX|Nifty 50,NSE_INDEX|Nifty Bank";

    public static String getQuotes(String keys) throws Exception {
        String finalKeys = sanitizeKeys(keys);

        if (finalKeys == null || finalKeys.isBlank()) {
            return "{\"error\":\"No keys provided\"}";
        }

        String cacheKey = "quotes:" + finalKeys;
        String cached = MemoryCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String rawResponse = UpstoxConnector.fetchLtpQuotes(finalKeys);

        if (rawResponse == null || rawResponse.isBlank()) {
            return "{\"error\":\"Empty response from upstream\"}";
        }

        if (rawResponse.contains("\"error\"")) {
            return rawResponse;
        }

        String normalized = normalizeResponse(rawResponse);

        MemoryCache.put(cacheKey, normalized);
        return normalized;
    }

    private static String sanitizeKeys(String keys) {
        if (keys == null || keys.isBlank()) {
            return DEFAULT_KEYS;
        }

        String[] parts = keys.split(",");
        List<String> cleaned = new ArrayList<>();

        for (String part : parts) {
            String value = part.trim();
            if (!value.isBlank()) {
                cleaned.add(value);
            }
        }

        if (cleaned.isEmpty()) {
            return null;
        }

        return String.join(",", cleaned);
    }

    private static String normalizeResponse(String rawJson) {
        StringBuilder result = new StringBuilder();
        List<String> items = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "\"([^\"]+)\"\\s*:\\s*\\{[^\\}]*?\"last_price\"\\s*:\\s*([0-9.]+)[^\\}]*?\"instrument_token\"\\s*:\\s*\"([^\"]+)\""
        );

        Matcher matcher = pattern.matcher(rawJson);

        while (matcher.find()) {
            String rawSymbol = matcher.group(1);
            String ltp = matcher.group(2);
            String instrument = matcher.group(3);

            String cleanSymbol = mapSymbol(rawSymbol, instrument);

            String item = "{"
                    + "\"symbol\":\"" + escapeJson(cleanSymbol) + "\","
                    + "\"ltp\":" + ltp + ","
                    + "\"instrument\":\"" + escapeJson(instrument) + "\""
                    + "}";

            items.add(item);
        }

        if (items.isEmpty()) {
            return "{\"error\":\"No quote data found\"}";
        }

        result.append("{");
        result.append("\"timestamp\":").append(System.currentTimeMillis()).append(",");
        result.append("\"count\":").append(items.size()).append(",");
        result.append("\"data\":[");
        result.append(String.join(",", items));
        result.append("]");
        result.append("}");

        return result.toString();
    }

    private static String mapSymbol(String rawSymbol, String instrument) {
        String combined = (rawSymbol + " " + instrument).toUpperCase();

        if (combined.contains("NIFTY BANK")) {
            return "BANKNIFTY";
        }

        if (combined.contains("NIFTY 50")) {
            return "NIFTY";
        }

        if (instrument.contains("|")) {
            String[] parts = instrument.split("\\|", 2);
            if (parts.length == 2 && !parts[1].isBlank()) {
                return parts[1].trim().toUpperCase().replace(" ", "_");
            }
        }

        if (rawSymbol.contains(":")) {
            return rawSymbol.substring(rawSymbol.indexOf(":") + 1).trim().toUpperCase().replace(" ", "_");
        }

        return rawSymbol.trim().toUpperCase().replace(" ", "_");
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
