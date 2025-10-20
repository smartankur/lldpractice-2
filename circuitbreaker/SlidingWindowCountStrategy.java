package circuitbreaker;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Sliding window counter strategy - more accurate than fixed window
 * Continuously tracks requests within the time window
 */
public class SlidingWindowCountStrategy implements IFailureCounterStrategy {
    
    private final ConcurrentLinkedDeque<RequestRecord> requestQueue;
    private final long windowDurationMs;
    
    /**
     * Record of a single request with timestamp
     */
    static class RequestRecord {
        final long timestamp;
        final boolean isFailure;
        
        RequestRecord(long timestamp, boolean isFailure) {
            this.timestamp = timestamp;
            this.isFailure = isFailure;
        }
    }
    
    public SlidingWindowCountStrategy(long windowDurationMs) {
        this.windowDurationMs = windowDurationMs;
        this.requestQueue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public int countFailures(CircuitBreakerContext circuitBreakerContext) {
        long currentTime = circuitBreakerContext.getCurrentTime().get();
        
        // Remove expired entries from the front
        removeExpiredEntries(currentTime);
        
        // Count failures in the valid window
        return (int) requestQueue.stream()
                .filter(r -> r.isFailure)
                .count();
    }

    public void recordFailure(CircuitBreakerContext circuitBreakerContext) {
        long currentTime = System.currentTimeMillis();
        circuitBreakerContext.getCurrentTime().set(currentTime);
        
        // Remove expired entries first
        removeExpiredEntries(currentTime);
        
        // Add new failure record to the back
        requestQueue.addLast(new RequestRecord(currentTime, true));
        
        // Update context for compatibility (HALF_OPEN state uses this)
        var windowContext = circuitBreakerContext.getWindowContext();
        windowContext.getTotalRequestsInCurrentWindow().set(getTotalRequests());
        windowContext.getCurrentWindowCount().set(countFailures(circuitBreakerContext));
        windowContext.getLastRequestedAtInMs().set(currentTime);
    }

    public void recordSuccess(CircuitBreakerContext circuitBreakerContext) {
        long currentTime = System.currentTimeMillis();
        circuitBreakerContext.getCurrentTime().set(currentTime);
        
        // Remove expired entries first
        removeExpiredEntries(currentTime);
        
        // Add new success record to the back
        requestQueue.addLast(new RequestRecord(currentTime, false));
        
        // Update context for compatibility (HALF_OPEN state uses this)
        var windowContext = circuitBreakerContext.getWindowContext();
        windowContext.getTotalRequestsInCurrentWindow().set(getTotalRequests());
        windowContext.getCurrentWindowCount().set(countFailures(circuitBreakerContext));
        windowContext.getLastRequestedAtInMs().set(currentTime);
    }
    
    /**
     * Remove records older than the window from the front of the queue
     * This is efficient because entries are naturally time-ordered
     */
    private void removeExpiredEntries(long currentTime) {
        long cutoffTime = currentTime - windowDurationMs;
        
        // Remove from front while entries are expired
        // ConcurrentLinkedDeque supports safe concurrent operations
        while (!requestQueue.isEmpty()) {
            RequestRecord first = requestQueue.peekFirst();
            if (first != null && first.timestamp < cutoffTime) {
                requestQueue.pollFirst();
            } else {
                break; // Rest are within the valid window
            }
        }
    }
    
    /**
     * Get total requests in current window (for metrics)
     */
    public long getTotalRequests() {
        return requestQueue.size();
    }
    
    /**
     * Clear all records (used when resetting state)
     */
    public void reset() {
        requestQueue.clear();
    }
}
