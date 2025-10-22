package expenserulemanagementsystem.conditionevaluator;

import expenserulemanagementsystem.model.Condition;

public class NotEqualsEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String fieldValue, Condition condition) {
        return !fieldValue.equals(condition.getComparisonValue());
    }
}