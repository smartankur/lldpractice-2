package expenserulemanagementsystem.comparisonstrategy;

public interface ComparisonStrategy {
    boolean compare(double actualValue, double thresholdValue);
}