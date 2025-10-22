package expenserulemanagementsystem.aggregationcalculator;

import java.util.Map;
import java.util.stream.Stream;

public class SumCalculator implements AggregationCalculator {
    @Override
    public double calculate(Stream<Map<String, String>> expenses, String fieldName) {
        return expenses
            .mapToDouble(e -> parseDouble(e.get(fieldName)))
            .sum();
    }
    
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
