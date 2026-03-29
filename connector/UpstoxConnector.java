package connector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UpstoxConnector {

    public static String fetchLtpQuotes(String keys) throws Exception {
        String accessToken = System.getenv("UPSTOX_ACCESS_TOKEN");

        if (accessToken == null || accessToken.isBlank()) {
            return "{\"error\":\"Missing token\"}";
        }

        if (keys == null || keys.isBlank()) {
            return "{\"error\":\"No keys provided\"}";
        }

        String apiUrl = "https://api.upstox.com/v2/market-quote/ltp?instrument_key="
                + URLEncoder.encode(keys, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int statusCode = conn.getResponseCode();

        BufferedReader reader;
        if (statusCode >= 200 && statusCode < 300) {
            reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
        } else {
            if (conn.getErrorStream() == null) {
                conn.disconnect();
                return "{\"error\":\"Upstream request failed with status " + statusCode + "\"}";
            }

            reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
            );
        }

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        conn.disconnect();

        String responseText = response.toString();

        if (statusCode >= 200 && statusCode < 300) {
            return responseText;
        }

        return "{\"error\":\"Upstream error\",\"status\":" + statusCode + ",\"details\":\""
                + escapeJson(responseText) + "\"}";
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
