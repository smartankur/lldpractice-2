package ratelimiter;

import java.util.concurrent.*;

public class FixedWindowCounterAlgorithm implements RateLimiter {

    private final RateLimiterConfig rateLimiterConfig;
    private final int maxRequests;
    private final long windowSizeMs;
    private final ConcurrentHashMap<String, Semaphore> window;
    private final ScheduledExecutorService scheduled;
    public FixedWindowCounterAlgorithm(RateLimiterConfig rateLimiterConfig) {
        this.rateLimiterConfig = rateLimiterConfig;
        this.maxRequests = rateLimiterConfig.getMaxRequests();
        this.windowSizeMs = rateLimiterConfig.getWindowSizeMs();
        window = new ConcurrentHashMap<>();
        scheduled = Executors.newSingleThreadScheduledExecutor();
        windowReset();
        startCleanupTask();
    }

    private void windowReset() {
        scheduled.scheduleAtFixedRate(() -> {
            try {
                window.forEach((key, semaphore) -> {
                    semaphore.drainPermits();
                    semaphore.release(maxRequests);
                });
            } catch (Exception e) {
                System.err.println("Error during window reset: " + e);
            }
        }, windowSizeMs, windowSizeMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean allowRequest(String identifier) {
        return window.computeIfAbsent(identifier, a-> new Semaphore(maxRequests)).tryAcquire();
    }

    @Override
    public boolean tryAllowRequest(String identifier, long timeoutMs) {
        try {
            return window.computeIfAbsent(identifier, a -> new Semaphore(maxRequests))
                    .tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void startCleanupTask() {
        scheduled.scheduleAtFixedRate(() -> {
            try {
                window.entrySet().removeIf(entry ->
                        entry.getValue().availablePermits() == maxRequests
                );
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }, windowSizeMs * 10, windowSizeMs * 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public int getCurrentCount(String identifier) {
        return window.get(identifier) == null ? 0:
                maxRequests - window.get(identifier).availablePermits();
    }

    @Override
    public int getRemainingRequests(String identifier) {
        return window.get(identifier) == null ? maxRequests: maxRequests - window.get(identifier).availablePermits();
    }

    @Override
    public void reset(String identifier) {
        Semaphore semaphore = window.get(identifier);
        if (semaphore != null) {
            semaphore.drainPermits();
            semaphore.release(maxRequests);
        }
    }

    @Override
    public void resetAll() {
        resetAllWindows();
    }

    private void resetAllWindows() {
        window.forEach((identifier, semaphore) -> {
            semaphore.drainPermits();
            semaphore.release(maxRequests);
        });
    }

    public void shutdown() {
        scheduled.shutdown();
        try {
            if (!scheduled.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduled.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduled.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
