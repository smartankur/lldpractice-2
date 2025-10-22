package expenserulemanagementsystem.aggregationcalculator;

import java.util.Map;
import java.util.stream.Stream;

public class CountCalculator implements AggregationCalculator {
    @Override
    public double calculate(Stream<Map<String, String>> expenses, String fieldName) {
        return expenses.count();
    }
}
