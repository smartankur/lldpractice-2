package circuitbreaker;

public interface IObserver {
    void registerMetrics(CircuitBreakerMetrics circuitBreakerMetrics);
}
