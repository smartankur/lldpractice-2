package expenserulemanagementsystem.comparisonstrategy;

import expenserulemanagementsystem.model.Operator;

import java.util.HashMap;
import java.util.Map;

public class ComparisonStrategyFactory {
    private static final Map<Operator, ComparisonStrategy> strategies = new HashMap<>();
    
    static {
        strategies.put(Operator.LESS_THAN, new LessThanComparison());
        strategies.put(Operator.LESS_THAN_OR_EQUAL, new LessThanOrEqualComparison());
        strategies.put(Operator.GREATER_THAN, new GreaterThanComparison());
        strategies.put(Operator.GREATER_THAN_OR_EQUAL, new GreaterThanOrEqualComparison());
        strategies.put(Operator.EQUALS, new EqualsComparison());
    }
    
    public static ComparisonStrategy getStrategy(Operator operator) {
        ComparisonStrategy strategy = strategies.get(operator);
        if (strategy == null) {
            throw new UnsupportedOperationException("Operator not supported for comparison: " + operator);
        }
        return strategy;
    }
}