package service;

import cache.MemoryCache;
import connector.UpstoxConnector;

public class QuotesService {

    public static String getQuotes(String symbols) throws Exception {

        if (symbols == null || symbols.isEmpty()) {
            symbols = "NSE_INDEX|Nifty 50,NSE_INDEX|Nifty Bank";
        }

        String cached = MemoryCache.get(symbols);

        if (cached != null) {
            return cached;
        }

        String data = UpstoxConnector.fetchQuotes(symbols);

        MemoryCache.put(symbols, data);

        return data;
    }
}
