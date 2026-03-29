package cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache {

    private static final Map<String, CacheItem> cache = new HashMap<>();
    private static final long TTL = 1000; // 1 second

    public static String get(String key) {
        CacheItem item = cache.get(key);

        if (item != null && (System.currentTimeMillis() - item.timestamp) < TTL) {
            return item.data;
        }

        return null;
    }

    public static void put(String key, String data) {
        cache.put(key, new CacheItem(data));
    }

    static class CacheItem {
        String data;
        long timestamp;

        CacheItem(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
