package circuitbreaker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class CircuitBreaker {

    private final CircuitBreakerConfig config;
    private final CircuitBreakerContext context;
    private final ConcurrentHashMap<CircuitBreakerState, CircuitBreakerStateManager> stateManagers;
    private final FixedWindowCountStrategy failureCounterStrategy;
    private IObserver observer;
    private final Object stateLock = new Object(); // Dedicated lock for state transitions

    public CircuitBreaker(CircuitBreakerConfig config, String user) {
        this.config = config;
        this.failureCounterStrategy = new FixedWindowCountStrategy();

        // Initialize context
        CircuitBreakerContext.WindowContext windowContext =
                CircuitBreakerContext.WindowContext.builder()
                        .currentWindowStartTime(new AtomicLong(System.currentTimeMillis()))
                        .currentWindowCount(new AtomicLong(0))
                        .totalRequestsInCurrentWindow(new AtomicLong(0))
                        .lastWindowCount(new AtomicLong(0))
                        .lastRequestedAtInMs(new AtomicLong(System.currentTimeMillis()))
                        .build();

        this.context = CircuitBreakerContext.builder()
                .circuitBreakerConfig(config)
                .user(user)
                .windowContext(windowContext)
                .currentTime(new AtomicLong(System.currentTimeMillis()))
                .currentState(CircuitBreakerState.CLOSED)
                .build();

        // Initialize state managers
        this.stateManagers = new ConcurrentHashMap<>();
        stateManagers.put(CircuitBreakerState.CLOSED,
                new CloseStateManager(failureCounterStrategy));

        OpenStateManager openStateManager = new OpenStateManager(failureCounterStrategy);
        openStateManager.setStateManagers(stateManagers);
        openStateManager.setStateLock(stateLock); // Share lock!
        stateManagers.put(CircuitBreakerState.OPEN, openStateManager);

        stateManagers.put(CircuitBreakerState.HALF_OPEN,
                new HalfOpenStateManager(failureCounterStrategy));

        // Initialize with CLOSED state
        getCurrentStateManager().onEnter(context);
    }

    public <T> T call(Supplier<T> supplier) throws CircuitBreakerOpenException {
        context.getCurrentTime().set(System.currentTimeMillis());

        // Check if request is allowed (doesn't need lock for reading volatile state)
        CircuitBreakerStateManager currentManager = getCurrentStateManager();
        if (!currentManager.allowRequest(context)) {
            throw new CircuitBreakerOpenException(
                    "Circuit breaker in " + context.getCurrentState() + " state - Request blocked");
        }

        try {
            T result = supplier.get();
            onSuccess();
            return result;

        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private void onSuccess() {
        context.getCurrentTime().set(System.currentTimeMillis());

        failureCounterStrategy.recordSuccess(context);

        System.out.println("✓ Success | State: " + context.getCurrentState() +
                " | Failures: " + failureCounterStrategy.countFailures(context) +
                " | Total: " + context.getWindowContext().getTotalRequestsInCurrentWindow().get());

        checkAndTransition();
    }

    private void onFailure() {
        context.getCurrentTime().set(System.currentTimeMillis());

        failureCounterStrategy.recordFailure(context);

        System.out.println("✗ Failure | State: " + context.getCurrentState() +
                " | Failures: " + failureCounterStrategy.countFailures(context) +
                " | Total: " + context.getWindowContext().getTotalRequestsInCurrentWindow().get());

        checkAndTransition();
    }

    private void checkAndTransition() {
        synchronized (stateLock) { // Synchronize state transitions
            CircuitBreakerState currentState = context.getCurrentState();
            CircuitBreakerStateManager currentManager = getCurrentStateManager();

            CircuitBreakerState nextState = currentManager.getNext(context);

            if (nextState != currentState) {
                transitionTo(nextState);
            }
        }
    }

    private void transitionTo(CircuitBreakerState nextState) {
        // Must be called within synchronized block!
        CircuitBreakerState currentState = context.getCurrentState();
        System.out.println("State transition: " + currentState + " → " + nextState);

        context.setCurrentState(nextState);

        CircuitBreakerStateManager nextManager = stateManagers.get(nextState);
        if (nextManager == null) {
            throw new IllegalStateException("No state manager found for: " + nextState);
        }
        nextManager.onEnter(context);

        if (observer != null) {
            observer.registerMetrics(new CircuitBreakerMetrics());
        }
    }

    private CircuitBreakerStateManager getCurrentStateManager() {
        CircuitBreakerStateManager manager = stateManagers.get(context.getCurrentState());
        if (manager == null) {
            throw new IllegalStateException("No state manager for: " + context.getCurrentState());
        }
        return manager;
    }

    public void registerObserver(IObserver observer) {
        this.observer = observer;
    }

    public CircuitBreakerState getCurrentState() {
        return context.getCurrentState();
    }

    public int getCurrentFailureCount() {
        return failureCounterStrategy.countFailures(context);
    }

    public void shutdown() {
        OpenStateManager openManager = (OpenStateManager) stateManagers.get(CircuitBreakerState.OPEN);
        if (openManager != null) {
            openManager.shutdown();
        }
    }
}