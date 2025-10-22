package expenserulemanagementsystem.aggregationcalculator;

import expenserulemanagementsystem.model.AggregationFunction;

import java.util.HashMap;
import java.util.Map;

public class AggregationCalculatorFactory {
    private static final Map<AggregationFunction, AggregationCalculator> calculators = new HashMap<>();
    
    static {
        calculators.put(AggregationFunction.SUM, new SumCalculator());
        calculators.put(AggregationFunction.COUNT, new CountCalculator());
        calculators.put(AggregationFunction.AVG, new AverageCalculator());
        calculators.put(AggregationFunction.MAX, new MaxCalculator());
        calculators.put(AggregationFunction.MIN, new MinCalculator());
    }
    
    public static AggregationCalculator getCalculator(AggregationFunction function) {
        AggregationCalculator calculator = calculators.get(function);
        if (calculator == null) {
            throw new UnsupportedOperationException("Aggregation function not supported: " + function);
        }
        return calculator;
    }
}
