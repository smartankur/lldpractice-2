package expenserulemanagementsystem;


import expenserulemanagementsystem.aggregationcalculator.AggregationCalculator;
import expenserulemanagementsystem.aggregationcalculator.AggregationCalculatorFactory;
import expenserulemanagementsystem.comparisonstrategy.ComparisonStrategy;
import expenserulemanagementsystem.comparisonstrategy.ComparisonStrategyFactory;
import expenserulemanagementsystem.conditionevaluator.ConditionEvaluator;
import expenserulemanagementsystem.conditionevaluator.ConditionEvaluatorFactory;
import expenserulemanagementsystem.model.Aggregation;
import expenserulemanagementsystem.model.Condition;
import expenserulemanagementsystem.model.Rule;
import expenserulemanagementsystem.model.RuleType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleEngine {
    private final RuleService ruleService;
    private final ConditionEvaluatorFactory conditionEvaluatorFactory;
    private final AggregationCalculatorFactory aggregationCalculatorFactory;
    private final ComparisonStrategyFactory comparisonStrategyFactory;

    public RuleEngine(RuleService ruleService) {
        this.ruleService = ruleService;
        this.conditionEvaluatorFactory = new ConditionEvaluatorFactory();
        this.aggregationCalculatorFactory = new AggregationCalculatorFactory();
        this.comparisonStrategyFactory = new ComparisonStrategyFactory();
    }

    public Map<String, List<RuleViolation>> evaluateExpenses(List<Map<String, String>> expenses) {
        List<Rule> rules = ruleService.getAllActiveRules();
        Map<String, List<RuleViolation>> violations = new HashMap<>();

        List<Rule> expenseRules = rules.stream()
                .filter(r -> r.getRuleType() == RuleType.EXPENSE_LEVEL)
                .toList();

        List<Rule> tripRules = rules.stream()
                .filter(r -> r.getRuleType() == RuleType.TRIP_LEVEL)
                .toList();

        // Evaluate expense-level rules
        for (Map<String, String> expense : expenses) {
            for (Rule rule : expenseRules) {
                if (!evaluateExpenseRule(rule, expense)) {
                    String expenseId = expense.get("expense_id");
                    violations.computeIfAbsent(expenseId, k -> new ArrayList<>())
                            .add(new RuleViolation(rule.getRuleId(), rule.getDescription()));
                }
            }
        }

        // Evaluate trip-level rules
        Map<String, List<Map<String, String>>> expensesByTrip = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.get("trip_id")));

        for (Map.Entry<String, List<Map<String, String>>> entry : expensesByTrip.entrySet()) {
            List<Map<String, String>> tripExpenses = entry.getValue();

            for (Rule rule : tripRules) {
                if (!evaluateTripRule(rule, tripExpenses)) {
                    for (Map<String, String> expense : tripExpenses) {
                        String expenseId = expense.get("expense_id");
                        violations.computeIfAbsent(expenseId, k -> new ArrayList<>())
                                .add(new RuleViolation(rule.getRuleId(), rule.getDescription()));
                    }
                }
            }
        }

        return violations;
    }

    /**
     * Evaluate expense-level rule using Strategy pattern
     * No switch statements - delegates to appropriate strategy
     */
    private boolean evaluateExpenseRule(Rule rule, Map<String, String> expense) {
        List<Condition> conditions = rule.getConditions();
        if (conditions.isEmpty()) return true;

        conditions.sort(Comparator.comparingInt(Condition::getOrder));

        // Start with neutral value based on first conjunction
        boolean result = true; // neutral for AND operations

        for (Condition condition : conditions) {
            boolean conditionResult = evaluateCondition(condition, expense);

            if (condition.getConjunction() == null || condition.getOrder() == 0) {
                // First condition or no conjunction specified
                result = conditionResult;
            } else if ("OR".equalsIgnoreCase(condition.getConjunction())) {
                result = result || conditionResult;
            } else { // AND
                result = result && conditionResult;
            }
        }

        return result;
    }
    /**
     * Evaluate a single condition using Strategy pattern
     * Delegates to appropriate ConditionEvaluator based on operator
     */
    private boolean evaluateCondition(Condition condition, Map<String, String> expense) {
        String fieldValue = expense.get(condition.getFieldName());  // amount or expense_type
        if (fieldValue == null) return false;

        // Get the appropriate strategy for this operator
        ConditionEvaluator evaluator = ConditionEvaluatorFactory.getEvaluator(condition.getOperator());

        // Delegate to strategy
        return evaluator.evaluate(fieldValue, condition);
    }

    /**
     * Evaluate trip-level rule using Strategy pattern
     * Uses AggregationCalculator and ComparisonStrategy
     */
    private boolean evaluateTripRule(Rule rule, List<Map<String, String>> tripExpenses) {
        Aggregation agg = rule.getAggregation();
        if (agg == null) return true;

        // Apply filter if specified
        Stream<Map<String, String>> stream = tripExpenses.stream();
        if (agg.getFilterField() != null && agg.getFilterValue() != null) {
            stream = stream.filter(e -> agg.getFilterValue().equals(e.get(agg.getFilterField())));
        }

        // Get the appropriate aggregation calculator strategy
        AggregationCalculator calculator = AggregationCalculatorFactory.getCalculator(agg.getFunction());

        // Calculate aggregated value using strategy
        double aggregatedValue = calculator.calculate(stream, agg.getFieldName());

        // Get the appropriate comparison strategy
        double threshold = parseDouble(agg.getThresholdValue());
        ComparisonStrategy comparisonStrategy = ComparisonStrategyFactory.getStrategy(agg.getOperator());

        // Compare using strategy
        return comparisonStrategy.compare(aggregatedValue, threshold);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

@Data
@AllArgsConstructor
class RuleViolation {
    private String ruleId;
    private String description;

    @Override
    public String toString() {
        return "Rule " + ruleId + ": " + description;
    }
}