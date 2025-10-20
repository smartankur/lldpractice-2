package circuitbreaker;

public class CloseStateManager extends CircuitBreakerStateManager {

    protected CloseStateManager(IFailureCounterStrategy failureCounterStrategy) {
        super(failureCounterStrategy);
    }

    @Override
    public CircuitBreakerState getStateName() {
        return CircuitBreakerState.CLOSED;
    }

    @Override
    public CircuitBreakerState getNext(CircuitBreakerContext circuitBreakerContext) {
        if (!circuitBreakerContext.getCurrentState().equals(CircuitBreakerState.CLOSED)) {
            throw new IllegalStateException("Internal state doesn't match with invoked method");
        }

        var countFailures = super.countFailures(circuitBreakerContext);
        if (countFailures >= circuitBreakerContext.getCircuitBreakerConfig().getThreshold()) {
            System.out.println("Failure threshold breached. Transitioning to OPEN");
            return CircuitBreakerState.OPEN;
        }
        return CircuitBreakerState.CLOSED;
    }

    @Override
    public void onEnter(CircuitBreakerContext circuitBreakerContext) {
        System.out.println("→ Entered CLOSED state - Normal operation");
        // Reset window context using proper atomic operations
        var windowContext = circuitBreakerContext.getWindowContext();
        windowContext.getCurrentWindowCount().set(0);
        windowContext.getLastWindowCount().set(0);
        windowContext.getTotalRequestsInCurrentWindow().set(0);
        windowContext.getCurrentWindowStartTime().set(System.currentTimeMillis());
    }

    @Override
    public boolean allowRequest(CircuitBreakerContext circuitBreakerContext) {
        return true;
    }
}