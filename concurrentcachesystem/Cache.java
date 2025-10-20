package concurrentcachesystem;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

// ==================== INTERFACES (PROVIDED) ====================

interface Cache<K, V> {
    /**
     * Store value with expiration time
     * @param key The key
     * @param value The value
     * @param ttlMillis Time to live in milliseconds (0 = no expiration)
     */
    void put(K key, V value, long ttlMillis);
    
    /**
     * Get value if exists and not expired
     * @return Value or null if not found/expired
     */
    V get(K key);
    
    /**
     * Get cache statistics (thread-safe)
     */
    CacheStats getStats();
    
    /**
     * Shutdown background threads
     */
    void shutdown();
}