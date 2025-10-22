package expenserulemanagementsystem.comparisonstrategy;

public class EqualsComparison implements ComparisonStrategy {
    @Override
    public boolean compare(double actualValue, double thresholdValue) {
        return Math.abs(actualValue - thresholdValue) < 0.001; // floating point comparison
    }
}