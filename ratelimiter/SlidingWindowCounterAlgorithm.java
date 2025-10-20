package ratelimiter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowCounterAlgorithm implements RateLimiter {

    private final int maxRequests;
    private final long windowSizeMs;
    private final ConcurrentHashMap<String, WindowData> windows;

    public SlidingWindowCounterAlgorithm(RateLimiterConfig rateLimiterConfig) {
        this.maxRequests = rateLimiterConfig.getMaxRequests();
        this.windowSizeMs = rateLimiterConfig.getWindowSizeMs();
        windows = new ConcurrentHashMap<>();
    }

    private static class WindowData {
        private final AtomicInteger currentWindowCount;
        private final AtomicInteger previousWindowCount;
        private volatile long currentWindowStart;
        private final ReentrantLock lock;

        public WindowData() {
            this.currentWindowCount = new AtomicInteger(0);
            this.previousWindowCount = new AtomicInteger(0);
            this.currentWindowStart = System.currentTimeMillis();
            this.lock = new ReentrantLock();
        }
    }

    @Override
    public boolean allowRequest(String identifier) {
        long now = System.currentTimeMillis();
        WindowData data = windows.computeIfAbsent(identifier, k -> new WindowData());

        data.lock.lock();
        try {
            slideWindowIfNeeded(data, now);

            long timeInCurrentWindow = now - data.currentWindowStart;
            double overlapPercentage = 1.0 - ((double) timeInCurrentWindow / windowSizeMs);
            overlapPercentage = Math.max(0, Math.min(1, overlapPercentage));

            int current = data.currentWindowCount.get();
            int previous = data.previousWindowCount.get();
            double weightedCount = current + (previous * overlapPercentage);

            if (weightedCount < maxRequests) {
                data.currentWindowCount.incrementAndGet();
                return true;
            }

            return false;
        } finally {
            data.lock.unlock();
        }
    }

    private void slideWindowIfNeeded(WindowData data, long now) {
        long timeSinceStart = now - data.currentWindowStart;
        if (timeSinceStart >= windowSizeMs) {
            data.previousWindowCount.set(data.currentWindowCount.get());
            data.currentWindowCount.set(0);
            data.currentWindowStart = now;
        }
    }

    @Override
    public boolean tryAllowRequest(String identifier, long timeoutMs) throws InterruptedException {
        long now = System.currentTimeMillis();
        WindowData data = windows.computeIfAbsent(identifier, k -> new WindowData());

        if (!data.lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
            return false;
        }
        try {
            slideWindowIfNeeded(data, now);

            long timeInCurrentWindow = now - data.currentWindowStart;
            double overlapPercentage = 1.0 - ((double) timeInCurrentWindow / windowSizeMs);
            overlapPercentage = Math.max(0, Math.min(1, overlapPercentage));

            int current = data.currentWindowCount.get();
            int previous = data.previousWindowCount.get();
            double weightedCount = current + (previous * overlapPercentage);

            if (weightedCount < maxRequests) {
                data.currentWindowCount.incrementAndGet();
                return true;
            }

            return false;
        } finally {
            data.lock.unlock();
        }
    }

    @Override
    public int getCurrentCount(String identifier) {
        WindowData data = windows.get(identifier);

        if (data == null) {
            return 0;
        }

        data.lock.lock();
        try {
            long now = System.currentTimeMillis();
            slideWindowIfNeeded(data, now);
            long timeInCurrentWindow = now - data.currentWindowStart;
            double overlapPercentage = 1.0 - ((double) timeInCurrentWindow / windowSizeMs);
            overlapPercentage = Math.max(0, Math.min(1, overlapPercentage));
            int current = data.currentWindowCount.get();
            int previous = data.previousWindowCount.get();
            double weightedCount = current + (previous * overlapPercentage);
            return (int) Math.ceil(weightedCount);

        } finally {
            data.lock.unlock();
        }
    }

    @Override
    public int getRemainingRequests(String identifier) {
        WindowData data = windows.get(identifier);
        if (data == null) {
            return maxRequests;
        }

        data.lock.lock();
        try {
            long now = System.currentTimeMillis();
            slideWindowIfNeeded(data, now);
            long timeInCurrentWindow = now - data.currentWindowStart;
            double overlapPercentage = 1.0 - ((double) timeInCurrentWindow / windowSizeMs);
            overlapPercentage = Math.max(0, Math.min(1, overlapPercentage));

            int current = data.currentWindowCount.get();
            int previous = data.previousWindowCount.get();
            double weightedCount = current + (previous * overlapPercentage);
            int remaining = (int) Math.floor(maxRequests - weightedCount);
            return Math.max(0, remaining);

        } finally {
            data.lock.unlock();
        }
    }

    @Override
    public void reset(String identifier) {
        WindowData data = windows.get(identifier);

        if (data != null) {
            data.lock.lock();
            try {
                data.currentWindowCount.set(0);
                data.previousWindowCount.set(0);

                data.currentWindowStart = System.currentTimeMillis();

            } finally {
                data.lock.unlock();
            }
        }
    }

    @Override
    public void resetAll() {
        long now = System.currentTimeMillis();

        windows.values().forEach(data -> {
            data.lock.lock();
            try {
                data.currentWindowCount.set(0);
                data.previousWindowCount.set(0);
                data.currentWindowStart = now;

            } finally {
                data.lock.unlock();
            }
        });
    }
}
