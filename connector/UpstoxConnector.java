package connector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UpstoxConnector {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;

    public static String fetchLtpQuotes(String keys) throws Exception {
        String accessToken = System.getenv("UPSTOX_ACCESS_TOKEN");

        if (accessToken == null || accessToken.isBlank()) {
            return "{\"success\":false,\"error\":\"Missing token\",\"source\":\"UPSTOX\"}";
        }

        if (keys == null || keys.isBlank()) {
            return "{\"success\":false,\"error\":\"No keys provided\",\"source\":\"UPSTOX\"}";
        }

        String apiUrl = "https://api.upstox.com/v2/market-quote/ltp?instrument_key="
                + URLEncoder.encode(keys, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
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
                return "{\"success\":false,\"error\":\"Upstream request failed\",\"status\":" + statusCode + ",\"source\":\"UPSTOX\"}";
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

        return "{"
                + "\"success\":false,"
                + "\"error\":\"Upstream error\","
                + "\"status\":" + statusCode + ","
                + "\"details\":\"" + escapeJson(responseText) + "\","
                + "\"source\":\"UPSTOX\""
                + "}";
    }

    public static String fetchText(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("Accept", "*/*");

        int statusCode = conn.getResponseCode();

        BufferedReader reader;
        if (statusCode >= 200 && statusCode < 300) {
            reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );
        } else {
            if (conn.getErrorStream() == null) {
                conn.disconnect();
                throw new RuntimeException("Master fetch failed with status " + statusCode);
            }
            reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
            );
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }

        reader.close();
        conn.disconnect();

        if (statusCode >= 200 && statusCode < 300) {
            return response.toString();
        }

        throw new RuntimeException("Master fetch failed: " + response);
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
