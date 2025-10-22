package expenserulemanagementsystem.conditionevaluator;

import expenserulemanagementsystem.model.Condition;

public class LessThanEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String fieldValue, Condition condition) {
        return parseDouble(fieldValue) < parseDouble(condition.getComparisonValue());
    }
    
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}