package expenserulemanagementsystem.comparisonstrategy;

public class LessThanOrEqualComparison implements ComparisonStrategy {
    @Override
    public boolean compare(double actualValue, double thresholdValue) {
        return actualValue <= thresholdValue;
    }
}