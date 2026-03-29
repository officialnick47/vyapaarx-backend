package cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache {
    private static final Map<String, CacheEntry> CACHE = new HashMap<>();
    private static final long TTL_MILLIS = 1000;

    public static synchronized String get(String key) {
        CacheEntry entry = CACHE.get(key);
        if (entry == null) return null;

        long age = System.currentTimeMillis() - entry.timestamp;
        if (age > TTL_MILLIS) {
            CACHE.remove(key);
            return null;
        }

        return entry.data;
    }

    public static synchronized void put(String key, String data) {
        CACHE.put(key, new CacheEntry(data, System.currentTimeMillis()));
    }

    private static class CacheEntry {
        String data;
        long timestamp;

        CacheEntry(String data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}
