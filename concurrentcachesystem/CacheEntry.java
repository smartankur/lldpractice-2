package concurrentcachesystem;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

final class CacheEntry<V> {
    // --- Core data ---
    volatile V value;
    volatile Node location;

    // Absolute expiration timestamp in millis; 0 means "never expires"
    volatile long expireAtMillis;

    // Last access time in nanoseconds (for finer LRU ordering)
    private final AtomicLong lastAccessNanos = new AtomicLong(System.nanoTime());

    private CacheEntry(V value, long expireAtMillis, Node n) {
        this.value = value;
        this.location = n;
        this.expireAtMillis = expireAtMillis;
    }

    static <K, V> CacheEntry<V> of(V value, long ttlMillis, long nowMillis, Node n) {
        long expireAt = (ttlMillis <= 0) ? 0L : (nowMillis + ttlMillis);
        return new CacheEntry<>(value, expireAt, n);
    }

    /** Returns true if this entry is expired at the given wall-clock millis. */
    boolean isExpired(long nowMillis) {
        long exp = expireAtMillis;
        return exp > 0 && nowMillis >= exp;
    }

    /** Update last-access timestamp (for LRU). Call on successful get(). */
    void touch() {
        lastAccessNanos.set(System.nanoTime());
    }

    /** Returns the last-access time in nanos (for tie-breaking / ordering). */
    long lastAccessNanos() {
        return lastAccessNanos.get();
    }

    /**
     * Replace value and TTL in one go (e.g., on put/overwrite).
     * Pass ttlMillis <= 0 for "no expiration".
     */
    void set(V newValue, long ttlMillis, long nowMillis) {
        this.value = newValue;
        this.expireAtMillis = (ttlMillis <= 0) ? 0L : (nowMillis + ttlMillis);
        touch();
    }

    /** Returns remaining TTL in millis (0 if none or already expired). */
    long remainingTtlMillis(long nowMillis) {
        long exp = expireAtMillis;
        if (exp == 0) return 0L;
        long rem = exp - nowMillis;
        return Math.max(rem, 0L);
    }

    @Override
    public String toString() {
        return "CacheEntry{value=" + value + ", expireAt=" + expireAtMillis + "}";
    }
}
