package expenserulemanagementsystem.comparisonstrategy;

public class GreaterThanOrEqualComparison implements ComparisonStrategy {
    @Override
    public boolean compare(double actualValue, double thresholdValue) {
        return actualValue >= thresholdValue;
    }
}