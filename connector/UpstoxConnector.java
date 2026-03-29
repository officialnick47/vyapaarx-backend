package connector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpstoxConnector {

    public static String fetchQuotes(String symbols) throws Exception {

        String token = System.getenv("UPSTOX_ACCESS_TOKEN");

        if (token == null || token.isEmpty()) {
            return "{\"error\":\"Missing token\"}";
        }

        String apiUrl = "https://api.upstox.com/v2/market-quote/quotes?instrument_key=" + symbols;

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();

        return response.toString();
    }
}
