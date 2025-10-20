package circuitbreaker;

public class HalfOpenStateManager extends CircuitBreakerStateManager {

    protected HalfOpenStateManager(IFailureCounterStrategy failureCounterStrategy) {
        super(failureCounterStrategy);
    }

    @Override
    public CircuitBreakerState getStateName() {
        return CircuitBreakerState.HALF_OPEN;
    }

    @Override
    public CircuitBreakerState getNext(CircuitBreakerContext circuitBreakerContext) {
        var config = circuitBreakerContext.getCircuitBreakerConfig();
        var windowContext = circuitBreakerContext.getWindowContext();

        long totalRequests = windowContext.getTotalRequestsInCurrentWindow().get();
        long failures = windowContext.getCurrentWindowCount().get();
        long successes = totalRequests - failures;

        System.out.println("  [HALF_OPEN] Total: " + totalRequests +
                ", Successes: " + successes +
                ", Failures: " + failures);

        // Check if too many failures
        if (failures >= config.getAllowedFailureRequestInHalfOpen()) {
            System.out.println("Too many failures in HALF_OPEN (" + failures +
                    " >= " + config.getAllowedFailureRequestInHalfOpen() +
                    "). Transitioning to OPEN");
            return CircuitBreakerState.OPEN;
        }

        // Check if we've collected enough samples
        if (totalRequests >= config.getAllowedRequestsInHalfOpen()) {
            long requiredSuccesses = config.getAllowedRequestsInHalfOpen() - config.getAllowedFailureRequestInHalfOpen();

            if (successes >= requiredSuccesses) {
                System.out.println("Enough successes in HALF_OPEN (" + successes +
                        " >= " + requiredSuccesses +
                        "). Transitioning to CLOSED");
                return CircuitBreakerState.CLOSED;
            } else {
                System.out.println("Not enough successes in HALF_OPEN. Transitioning to OPEN");
                return CircuitBreakerState.OPEN;
            }
        }

        return CircuitBreakerState.HALF_OPEN;
    }

    @Override
    public void onEnter(CircuitBreakerContext circuitBreakerContext) {
        System.out.println("→ Entered HALF_OPEN state - Testing with limited requests");
        var windowContext = circuitBreakerContext.getWindowContext();
        windowContext.getCurrentWindowCount().set(0);
        windowContext.getLastWindowCount().set(0);
        windowContext.getTotalRequestsInCurrentWindow().set(0);
        windowContext.getCurrentWindowStartTime().set(System.currentTimeMillis());
    }

    @Override
    public boolean allowRequest(CircuitBreakerContext circuitBreakerContext) {
        long totalRequests = circuitBreakerContext.getWindowContext().getTotalRequestsInCurrentWindow().get();
        int allowedRequests = circuitBreakerContext.getCircuitBreakerConfig().getAllowedRequestsInHalfOpen();

        return totalRequests < allowedRequests;
    }
}