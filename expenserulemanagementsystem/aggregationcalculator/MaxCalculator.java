package expenserulemanagementsystem.aggregationcalculator;

import java.util.Map;
import java.util.stream.Stream;

public class MaxCalculator implements AggregationCalculator {
    @Override
    public double calculate(Stream<Map<String, String>> expenses, String fieldName) {
        return expenses
            .mapToDouble(e -> parseDouble(e.get(fieldName)))
            .max()
            .orElse(0.0);
    }
    
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
