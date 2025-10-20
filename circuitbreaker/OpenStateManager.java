package circuitbreaker;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Map;

public class OpenStateManager extends CircuitBreakerStateManager {
    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledTransition;
    private Map<CircuitBreakerState, CircuitBreakerStateManager> stateManagers;
    private Object stateLock; // Lock for state transitions

    protected OpenStateManager(IFailureCounterStrategy failureCounterStrategy) {
        super(failureCounterStrategy);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void setStateManagers(Map<CircuitBreakerState, CircuitBreakerStateManager> stateManagers) {
        this.stateManagers = stateManagers;
    }

    public void setStateLock(Object lock) {
        this.stateLock = lock;
    }

    @Override
    public CircuitBreakerState getStateName() {
        return CircuitBreakerState.OPEN;
    }

    @Override
    public CircuitBreakerState getNext(CircuitBreakerContext circuitBreakerContext) {
        return CircuitBreakerState.OPEN;
    }

    @Override
    public void onEnter(CircuitBreakerContext circuitBreakerContext) {
        System.out.println("→ Entered OPEN state - Blocking all requests for " +
                circuitBreakerContext.getCircuitBreakerConfig().getTimeoutInMs() + "ms");

        if (scheduledTransition != null && !scheduledTransition.isDone()) {
            scheduledTransition.cancel(false);
        }

        // Schedule transition with proper synchronization
        scheduledTransition = scheduledExecutorService.schedule(() -> {
            synchronized (stateLock) { // Use same lock as main circuit breaker!
                if (circuitBreakerContext.getCurrentState() == CircuitBreakerState.OPEN) {
                    System.out.println("⏰ Timeout elapsed. Auto-transitioning: OPEN → HALF_OPEN");

                    circuitBreakerContext.setCurrentState(CircuitBreakerState.HALF_OPEN);

                    CircuitBreakerStateManager halfOpenManager = stateManagers.get(CircuitBreakerState.HALF_OPEN);
                    if (halfOpenManager != null) {
                        halfOpenManager.onEnter(circuitBreakerContext);
                    }
                }
            }
        }, circuitBreakerContext.getCircuitBreakerConfig().getTimeoutInMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean allowRequest(CircuitBreakerContext circuitBreakerContext) {
        return false;
    }

    public void shutdown() {
        if (scheduledTransition != null && !scheduledTransition.isDone()) {
            scheduledTransition.cancel(false);
        }
        scheduledExecutorService.shutdown();
    }
}