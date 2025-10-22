package expenserulemanagementsystem;

import expenserulemanagementsystem.model.*;
import expenserulemanagementsystem.repository.InMemoryRuleRepository;
import expenserulemanagementsystem.repository.RuleRepository;

import java.util.*;

public class Solution {
    
    public static void main(String[] args) {
        // Initialize components
        RuleRepository repository = new InMemoryRuleRepository();
        RuleService ruleService = new RuleService(repository);
        RuleEngine ruleEngine = new RuleEngine(ruleService);
        
        // Create rules via "API"
        createSampleRules(ruleService);
        
        // Prepare expenses
        List<Map<String, String>> expenses = createSampleExpenses();
        
        // Evaluate expenses
        Map<String, List<RuleViolation>> violations = ruleEngine.evaluateExpenses(expenses);
        
        // Print results
        printResults(expenses, violations);
        
        // Demo: Update rule
        System.out.println("\n=== Updating Restaurant Limit from $75 to $100 ===\n");
        updateRestaurantLimit(ruleService);
        
        // Re-evaluate
        violations = ruleEngine.evaluateExpenses(expenses);
        printUpdatedResults(expenses, violations);
    }
    
    private static void createSampleRules(RuleService ruleService) {
        // Rule 1: Amount limit $250
        RuleCreateRequest rule1 = RuleCreateRequest.builder()
            .ruleName("General Amount Limit")
            .description("No expense can exceed $250")
            .ruleType("EXPENSE_LEVEL")
            .conditions(Arrays.asList(
                ConditionRequest.builder()
                    .fieldName("amount_usd")
                    .operator("GREATER_THAN")
                    .comparisonValue("250")
                    .build()
            ))
            .build();
        ruleService.createRule(rule1);
        
        // Rule 2: No airfare or entertainment
        RuleCreateRequest rule2 = RuleCreateRequest.builder()
            .ruleName("Restricted Expense Types")
            .description("Airfare and entertainment expenses are not allowed")
            .ruleType("EXPENSE_LEVEL")
            .conditions(Arrays.asList(
                ConditionRequest.builder()
                    .fieldName("expense_type")
                    .operator("IN")
                    .comparisonValues(Arrays.asList("airfare", "entertainment"))
                    .build()
            ))
            .build();
        ruleService.createRule(rule2);
        
        // Rule 3: Restaurant limit $75
        RuleCreateRequest rule3 = RuleCreateRequest.builder()
            .ruleName("Restaurant Expense Limit")
            .description("Restaurant expenses cannot exceed $75")
            .ruleType("EXPENSE_LEVEL")
            .conditions(Arrays.asList(
                ConditionRequest.builder()
                    .fieldName("expense_type")
                    .operator("EQUALS")
                    .comparisonValue("restaurant")
                    .conjunction("AND")
                    .build(),
                ConditionRequest.builder()
                    .fieldName("amount_usd")
                    .operator("GREATER_THAN")
                    .comparisonValue("75")
                    .build()
            ))
            .build();
        ruleService.createRule(rule3);
        
        // Rule 4: Trip total limit $2000
        RuleCreateRequest rule4 = RuleCreateRequest.builder()
            .ruleName("Trip Total Limit")
            .description("Total expenses for a trip cannot exceed $2000")
            .ruleType("TRIP_LEVEL")
            .aggregation(AggregationRequest.builder()
                .function("SUM")
                .fieldName("amount_usd")
                .operator("GREATER_THAN")
                .thresholdValue("2000")
                .build())
            .build();
        ruleService.createRule(rule4);
        
        System.out.println("✓ Created 4 sample rules in repository\n");
    }
    
    private static void updateRestaurantLimit(RuleService ruleService) {
        List<RuleResponse> allRules = ruleService.getAllRules();
        RuleResponse restaurantRule = allRules.stream()
            .filter(r -> r.getRuleName().equals("Restaurant Expense Limit"))
            .findFirst()
            .orElseThrow();
        
        RuleUpdateRequest updateRequest = RuleUpdateRequest.builder()
            .conditions(Arrays.asList(
                ConditionRequest.builder()
                    .fieldName("expense_type")
                    .operator("EQUALS")
                    .comparisonValue("restaurant")
                    .conjunction("AND")
                    .build(),
                ConditionRequest.builder()
                    .fieldName("amount_usd")
                    .operator("GREATER_THAN")
                    .comparisonValue("100") // Changed from 75
                    .build()
            ))
            .build();
        
        ruleService.updateRule(restaurantRule.getRuleId(), updateRequest);
        System.out.println("✓ Updated restaurant limit to $100\n");
    }
    
    private static List<Map<String, String>> createSampleExpenses() {
        List<Map<String, String>> expenses = new ArrayList<>();
        
        expenses.add(new HashMap<String, String>() {{
            put("expense_id", "001");
            put("trip_id", "001");
            put("amount_usd", "49.5");
            put("expense_type", "restaurant");
        }});
        
        expenses.add(new HashMap<String, String>() {{
            put("expense_id", "002");
            put("trip_id", "001");
            put("amount_usd", "275");
            put("expense_type", "airfare");
        }});
        
        expenses.add(new HashMap<String, String>() {{
            put("expense_id", "003");
            put("trip_id", "002");
            put("amount_usd", "65");
            put("expense_type", "entertainment");
        }});
        
        expenses.add(new HashMap<String, String>() {{
            put("expense_id", "004");
            put("trip_id", "003");
            put("amount_usd", "95.3");
            put("expense_type", "restaurant");
        }});
        
        return expenses;
    }
    
    private static void printResults(List<Map<String, String>> expenses, 
                                     Map<String, List<RuleViolation>> violations) {
        System.out.println("=== Expense Evaluation Results ===\n");
        for (Map<String, String> expense : expenses) {
            String expenseId = expense.get("expense_id");
            System.out.println("Expense #" + expenseId + " (" + 
                expense.get("expense_type") + ", $" + expense.get("amount_usd") + ")");
            
            if (violations.containsKey(expenseId)) {
                for (RuleViolation violation : violations.get(expenseId)) {
                    System.out.println("  ❌ " + violation.getDescription());
                }
            } else {
                System.out.println("  ✓ Passed all rules");
            }
            System.out.println();
        }
    }
    
    private static void printUpdatedResults(List<Map<String, String>> expenses,
                                           Map<String, List<RuleViolation>> violations) {
        System.out.println("After updating rule:\n");
        String expense4Id = "004";
        System.out.println("Expense #" + expense4Id + " (restaurant, $95.3)");
        if (violations.containsKey(expense4Id)) {
            for (RuleViolation violation : violations.get(expense4Id)) {
                System.out.println("  ❌ " + violation.getDescription());
            }
        } else {
            System.out.println("  ✓ Passed all rules (limit increased to $100)");
        }
    }
}