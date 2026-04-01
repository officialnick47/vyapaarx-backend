package cache;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryCache {

    private static final ConcurrentHashMap<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    public static class CacheEntry {
        public final String value;
        public final long createdAt;
        public final long ttlMillis;

        public CacheEntry(String value, long ttlMillis) {
            this.value = value;
            this.createdAt = System.currentTimeMillis();
            this.ttlMillis = ttlMillis;
        }

        public boolean isFresh() {
            return System.currentTimeMillis() - createdAt <= ttlMillis;
        }

        public boolean isUsableAsStale(long maxStaleMillis) {
            return System.currentTimeMillis() - createdAt <= maxStaleMillis;
        }
    }

    public static String getFresh(String key) {
        CacheEntry entry = CACHE.get(key);
        if (entry == null) return null;
        return entry.isFresh() ? entry.value : null;
    }

    public static String getStale(String key, long maxStaleMillis) {
        CacheEntry entry = CACHE.get(key);
        if (entry == null) return null;
        return entry.isUsableAsStale(maxStaleMillis) ? entry.value : null;
    }

    public static void put(String key, String value, long ttlMillis) {
        CACHE.put(key, new CacheEntry(value, ttlMillis));
    }
}
