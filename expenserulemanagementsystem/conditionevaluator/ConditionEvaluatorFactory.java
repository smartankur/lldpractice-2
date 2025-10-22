package expenserulemanagementsystem.conditionevaluator;

import expenserulemanagementsystem.model.Operator;
import expenserulemanagementsystem.aggregationcalculator.GreaterThanOrEqualEvaluator;

import java.util.HashMap;
import java.util.Map;

public class ConditionEvaluatorFactory {
    private static final Map<Operator, ConditionEvaluator> evaluators = new HashMap<>();
    
    static {
        evaluators.put(Operator.EQUALS, new EqualsEvaluator());
        evaluators.put(Operator.NOT_EQUALS, new NotEqualsEvaluator());
        evaluators.put(Operator.LESS_THAN, new LessThanEvaluator());
        evaluators.put(Operator.LESS_THAN_OR_EQUAL, new LessThanOrEqualEvaluator());
        evaluators.put(Operator.GREATER_THAN, new GreaterThanEvaluator());
        evaluators.put(Operator.GREATER_THAN_OR_EQUAL, new GreaterThanOrEqualEvaluator());
        evaluators.put(Operator.IN, new InEvaluator());
        evaluators.put(Operator.NOT_IN, new NotInEvaluator());
        evaluators.put(Operator.CONTAINS, new ContainsEvaluator());
        evaluators.put(Operator.NOT_CONTAINS, new NotContainsEvaluator());
    }
    
    public static ConditionEvaluator getEvaluator(Operator operator) {
        ConditionEvaluator evaluator = evaluators.get(operator);
        if (evaluator == null) {
            throw new UnsupportedOperationException("Operator not supported: " + operator);
        }
        return evaluator;
    }
}