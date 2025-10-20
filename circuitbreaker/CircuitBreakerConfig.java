package circuitbreaker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CircuitBreakerConfig {
    private final int threshold;
    private final long timeoutInMs;
    private final long windowForFailureCount;
    private final int allowedRequestsInHalfOpen;
    private final int allowedFailureRequestInHalfOpen;
}
