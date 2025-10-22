package expenserulemanagementsystem.conditionevaluator;


import expenserulemanagementsystem.model.Condition;

public interface ConditionEvaluator {
    boolean evaluate(String fieldValue, Condition condition);
}