package circuitbreaker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
@Data
@Builder
public class CircuitBreakerContext {
    private CircuitBreakerConfig circuitBreakerConfig;
    private String user;
    private WindowContext windowContext;
    private AtomicLong currentTime;
    private volatile CircuitBreakerState currentState; // volatile is key!

    @Data
    @AllArgsConstructor
    @Builder
    public static class WindowContext {
        private AtomicLong lastWindowCount;
        private AtomicLong currentWindowStartTime;
        private AtomicLong currentWindowCount;
        private AtomicLong lastRequestedAtInMs;
        private AtomicLong totalRequestsInCurrentWindow;
    }
}