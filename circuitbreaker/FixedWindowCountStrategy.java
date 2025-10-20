package circuitbreaker;

public class FixedWindowCountStrategy implements IFailureCounterStrategy {

    @Override
    public int countFailures(CircuitBreakerContext circuitBreakerContext) {
        var windowContext = circuitBreakerContext.getWindowContext();
        var config = circuitBreakerContext.getCircuitBreakerConfig();

        long currentTime = circuitBreakerContext.getCurrentTime().get();
        long windowStartTime = windowContext.getCurrentWindowStartTime().get();
        long windowDuration = config.getWindowForFailureCount();

        // Check if current window has expired
        if (currentTime - windowStartTime >= windowDuration) {
            // Use CAS to ensure only one thread resets the window
            if (windowContext.getCurrentWindowStartTime().compareAndSet(windowStartTime, currentTime)) {
                // This thread won the race to reset
                long oldCount = windowContext.getCurrentWindowCount().getAndSet(0);
                windowContext.getLastWindowCount().set(oldCount);
                windowContext.getTotalRequestsInCurrentWindow().set(0);
            }
            return 0;
        }

        return Math.toIntExact(windowContext.getCurrentWindowCount().get());
    }

    public void recordFailure(CircuitBreakerContext circuitBreakerContext) {
        var windowContext = circuitBreakerContext.getWindowContext();
        long currentTime = System.currentTimeMillis();
        circuitBreakerContext.getCurrentTime().set(currentTime);

        // Check if window expired before recording
        countFailures(circuitBreakerContext);

        // Atomically increment counters
        windowContext.getCurrentWindowCount().incrementAndGet();
        windowContext.getTotalRequestsInCurrentWindow().incrementAndGet();
        windowContext.getLastRequestedAtInMs().set(currentTime);
    }

    public void recordSuccess(CircuitBreakerContext circuitBreakerContext) {
        var windowContext = circuitBreakerContext.getWindowContext();
        long currentTime = System.currentTimeMillis();
        circuitBreakerContext.getCurrentTime().set(currentTime);

        // Check if window expired before recording
        countFailures(circuitBreakerContext);

        // Only increment total request count (not failure count)
        windowContext.getTotalRequestsInCurrentWindow().incrementAndGet();
        windowContext.getLastRequestedAtInMs().set(currentTime);
    }
}