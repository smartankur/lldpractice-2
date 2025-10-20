package ratelimiter;

import java.util.concurrent.*;

public class LeakyBucketAlgorithm implements RateLimiter {

    private final int leakRate;
    private final int queueCapacity;
    private final ConcurrentHashMap<String, BlockingQueue<Long>> buckets;
    private final ScheduledExecutorService scheduler;

    public LeakyBucketAlgorithm(RateLimiterConfig config) {
        this.leakRate = config.getMaxRequests();
        this.queueCapacity = config.getBucketCapacity();
        this.buckets = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startLeaking();
    }

    private void startLeaking() {
        long periodMs = 1000 / leakRate;
        scheduler.scheduleAtFixedRate(() -> {
            try {
                leak();
            } catch (Exception e) {
                System.err.println("Leak error: " + e.getMessage());
            }
        }, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    private void leak() {
        buckets.forEach((identifier, queue) -> {
            queue.poll();
        });
    }

    @Override
    public boolean allowRequest(String identifier) {
        BlockingQueue<Long> bucket = buckets.computeIfAbsent(
                identifier,
                k -> new LinkedBlockingQueue<>(queueCapacity)
        );

        long timestamp = System.currentTimeMillis();
        return bucket.offer(timestamp);
    }

    @Override
    public boolean tryAllowRequest(String identifier, long timeoutMs) {
        BlockingQueue<Long> bucket = buckets.computeIfAbsent(
                identifier,
                k -> new LinkedBlockingQueue<>(queueCapacity)
        );

        try {
            long timestamp = System.currentTimeMillis();
            return bucket.offer(timestamp, timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public int getCurrentCount(String identifier) {
        BlockingQueue<Long> bucket = buckets.get(identifier);
        return bucket != null ? bucket.size() : 0;
    }

    @Override
    public int getRemainingRequests(String identifier) {
        BlockingQueue<Long> bucket = buckets.get(identifier);
        if (bucket == null) {
            return queueCapacity;
        }
        return queueCapacity - bucket.size();
    }

    @Override
    public void reset(String identifier) {
        BlockingQueue<Long> bucket = buckets.get(identifier);
        if (bucket != null) {
            bucket.clear();
        }
    }

    @Override
    public void resetAll() {
        buckets.values().forEach(BlockingQueue::clear);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}