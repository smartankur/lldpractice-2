package ratelimiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class RateLimiterConfig {
    private final int maxRequests;
    private final long windowSizeMs;
    private final RateLimitStrategy strategy;

    // Token Bucket specific
    private final int bucketCapacity;
    private final double refillRate;
    // Getters and builder...
}

