package expenserulemanagementsystem.conditionevaluator;

import expenserulemanagementsystem.model.Condition;

public class ContainsEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String fieldValue, Condition condition) {
        return fieldValue.contains(condition.getComparisonValue());
    }
}