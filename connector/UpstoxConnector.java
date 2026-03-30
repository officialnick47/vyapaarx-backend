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
        if (accessToken == null) return "{\"error\":\"Missing Token\"}";

        String apiUrl = "https://api.upstox.com/v2/market-quote/ltp?instrument_key=" 
                        + URLEncoder.encode(keys, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream()
        ));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        
        return sb.toString();
    }
}
