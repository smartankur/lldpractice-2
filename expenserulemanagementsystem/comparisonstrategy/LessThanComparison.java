package expenserulemanagementsystem.comparisonstrategy;

public class LessThanComparison implements ComparisonStrategy {
    @Override
    public boolean compare(double actualValue, double thresholdValue) {
        return actualValue < thresholdValue;
    }
}