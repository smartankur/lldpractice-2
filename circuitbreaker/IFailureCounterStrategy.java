package circuitbreaker;

public interface IFailureCounterStrategy {
    int countFailures(CircuitBreakerContext circuitBreakerContext);
}
