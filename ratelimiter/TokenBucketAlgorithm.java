package ratelimiter;

import java.util.concurrent.*;

public class TokenBucketAlgorithm implements RateLimiter {

    private final int bucketCapacity;
    private final double refillRate;
    private final ConcurrentHashMap<String, Semaphore> buckets;
    private final ScheduledExecutorService scheduler;

    public TokenBucketAlgorithm(RateLimiterConfig config) {
        this.bucketCapacity = config.getBucketCapacity();
        this.refillRate = config.getRefillRate();
        this.buckets = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startRefillTask();
    }

    private void startRefillTask() {
        scheduler.scheduleAtFixedRate(() -> {
            for (var tokens : buckets.values()) {
                if (tokens.availablePermits() < bucketCapacity) {
                    tokens.release();
                }
                tokens.release();
            }
        }, 0, (long) refillRate, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean allowRequest(String identifier) {
        Semaphore b = buckets.computeIfAbsent(
                identifier,
                k -> new Semaphore(bucketCapacity)
        );
        return b.tryAcquire();
    }

    @Override
    public boolean tryAllowRequest(String identifier, long timeoutMs) {
        Semaphore bucket = buckets.computeIfAbsent(
                identifier,
                k -> new Semaphore(bucketCapacity)
        );

        try {
            return bucket.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public int getCurrentCount(String identifier) {
        Semaphore bucket = buckets.get(identifier);
        if (bucket == null) return 0;
        return bucketCapacity - bucket.availablePermits();
    }

    @Override
    public int getRemainingRequests(String identifier) {
        Semaphore bucket = buckets.get(identifier);
        return bucket != null ? bucket.availablePermits() : bucketCapacity;
    }

    @Override
    public void reset(String identifier) {
        Semaphore bucket = buckets.get(identifier);
        if (bucket != null) {
            bucket.drainPermits();
            bucket.release(bucketCapacity);
        }
    }

    @Override
    public void resetAll() {
        buckets.clear();
    }
}
