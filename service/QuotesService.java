package service;

import cache.MemoryCache;
import connector.UpstoxConnector;

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

        String response = UpstoxConnector.fetchLtpQuotes(keys);

        if (!response.contains("\"error\"")) {
            MemoryCache.put(cacheKey, response);
        }

        return response;
    }
}
