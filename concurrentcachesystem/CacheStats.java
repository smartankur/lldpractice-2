package concurrentcachesystem;

import lombok.Builder;

@Builder
class CacheStats {
    public long hits;
    public long misses;
    public long evictions;
    public long expirations;
    public int currentSize;
    
    public double hitRate() {
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total * 100;
    }
}