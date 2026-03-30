package cache;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryCache {
    private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();

    public static String get(String key) {
        return CACHE.get(key);
    }

    public static void put(String key, String data) {
        CACHE.put(key, data);
    }
}
