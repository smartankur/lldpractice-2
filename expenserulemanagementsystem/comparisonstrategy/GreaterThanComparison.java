package expenserulemanagementsystem.comparisonstrategy;

public class GreaterThanComparison implements ComparisonStrategy {
    @Override
    public boolean compare(double actualValue, double thresholdValue) {
        return actualValue > thresholdValue;
    }
}