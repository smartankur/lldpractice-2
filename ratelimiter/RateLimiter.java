package ratelimiter;

public interface RateLimiter {
    /**
     * Check if request is allowed for the given identifier
     * 
     * @param identifier User ID, API key, or IP address
     * @return true if request allowed, false if rate limit exceeded
     * @throws InterruptedException if interrupted while waiting
     */
    boolean allowRequest(String identifier) throws InterruptedException;
    
    /**
     * Try to allow request with timeout
     * 
     * @param identifier User ID, API key, or IP address
     * @param timeoutMs Maximum time to wait in milliseconds
     * @return true if request allowed within timeout, false otherwise
     */
    boolean tryAllowRequest(String identifier, long timeoutMs) throws InterruptedException;
    
    /**
     * Get current request count for identifier
     * Thread-safe read operation
     * 
     * @param identifier User ID, API key, or IP address
     * @return Current number of requests in current window
     */
    int getCurrentCount(String identifier);
    
    /**
     * Get remaining requests allowed in current window
     * 
     * @param identifier User ID, API key, or IP address
     * @return Number of requests still allowed
     */
    int getRemainingRequests(String identifier);
    
    /**
     * Reset rate limit for specific identifier
     * Useful for testing or admin operations
     * 
     * @param identifier User ID to reset
     */
    void reset(String identifier);
    
    /**
     * Reset all rate limits
     * Use with caution in production!
     */
    void resetAll();
}