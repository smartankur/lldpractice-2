package expenserulemanagementsystem.conditionevaluator;

import expenserulemanagementsystem.model.Condition;

public class InEvaluator implements ConditionEvaluator {
    @Override
    public boolean evaluate(String fieldValue, Condition condition) {
        return condition.getComparisonValues().contains(fieldValue);
    }
}