package expenserulemanagementsystem.conditionevaluator;

import expenserulemanagementsystem.model.Condition;

public class NotContainsEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String fieldValue, Condition condition) {
        return !fieldValue.contains(condition.getComparisonValue());
    }
}
