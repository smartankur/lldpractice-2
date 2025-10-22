package expenserulemanagementsystem.aggregationcalculator;

import java.util.Map;
import java.util.stream.Stream;

public interface AggregationCalculator {
    double calculate(Stream<Map<String, String>> expenses, String fieldName);
}