package circuitbreaker;

public abstract class CircuitBreakerStateManager {

    private final IFailureCounterStrategy failureCounterStrategy;

    protected CircuitBreakerStateManager(IFailureCounterStrategy failureCounterStrategy) {
        this.failureCounterStrategy = failureCounterStrategy;
    }

    public abstract CircuitBreakerState getStateName();

    /**
     * Determines the next state based on current context
     */
    public abstract CircuitBreakerState getNext(CircuitBreakerContext circuitBreakerContext);

    /**
     * Called when entering this state
     */
    public abstract void onEnter(CircuitBreakerContext circuitBreakerContext);

    /**
     * Checks if requests should be allowed in this state
     */
    public abstract boolean allowRequest(CircuitBreakerContext circuitBreakerContext);

    public int countFailures(CircuitBreakerContext circuitBreakerContext) {
        return failureCounterStrategy.countFailures(circuitBreakerContext);
    }
}