package expenserulemanagementsystem;

import expenserulemanagementsystem.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class RuleMapper {
    
    public static Rule toEntity(RuleCreateRequest request) {
        Rule rule = new Rule(
            UUID.randomUUID().toString(),
            request.getRuleName(),
            request.getDescription(),
            RuleType.valueOf(request.getRuleType().toUpperCase())
        );
        
        // Map conditions
        if (request.getConditions() != null) {
            List<Condition> conditions = new ArrayList<>();
            for (int i = 0; i < request.getConditions().size(); i++) {
                ConditionRequest condReq = request.getConditions().get(i);
                Condition condition = Condition.builder()
                    .conditionId(UUID.randomUUID().toString())
                    .fieldName(condReq.getFieldName())
                    .operator(Operator.valueOf(condReq.getOperator().toUpperCase()))
                    .comparisonValue(condReq.getComparisonValue())
                    .comparisonValues(condReq.getComparisonValues() != null ? 
                        condReq.getComparisonValues() : new ArrayList<>())
                    .conjunction(condReq.getConjunction())
                    .order(i)
                    .build();
                conditions.add(condition);
            }
            rule.setConditions(conditions);
        }
        
        // Map aggregation (for trip-level rules)
        if (request.getAggregation() != null) {
            AggregationRequest aggReq = request.getAggregation();
            Aggregation aggregation = Aggregation.builder()
                .aggregationId(UUID.randomUUID().toString())
                .function(AggregationFunction.valueOf(aggReq.getFunction().toUpperCase()))
                .fieldName(aggReq.getFieldName())
                .filterField(aggReq.getFilterField())
                .filterValue(aggReq.getFilterValue())
                .operator(Operator.valueOf(aggReq.getOperator().toUpperCase()))
                .thresholdValue(aggReq.getThresholdValue())
                .build();
            rule.setAggregation(aggregation);
        }
        
        return rule;
    }
    
    public static RuleResponse toResponse(Rule rule) {
        RuleResponse.RuleResponseBuilder builder = RuleResponse.builder()
            .ruleId(rule.getRuleId())
            .ruleName(rule.getRuleName())
            .description(rule.getDescription())
            .ruleType(rule.getRuleType().name())
            .active(rule.isActive())
            .createdAt(rule.getCreatedAt())
            .updatedAt(rule.getUpdatedAt());
        
        // Map conditions
        if (rule.getConditions() != null) {
            List<ConditionResponse> conditionResponses = rule.getConditions().stream()
                .map(condition -> ConditionResponse.builder()
                    .fieldName(condition.getFieldName())
                    .operator(condition.getOperator().name())
                    .comparisonValue(condition.getComparisonValue())
                    .comparisonValues(condition.getComparisonValues())
                    .conjunction(condition.getConjunction())
                    .build())
                .collect(Collectors.toList());
            builder.conditions(conditionResponses);
        }
        
        // Map aggregation
        if (rule.getAggregation() != null) {
            Aggregation agg = rule.getAggregation();
            AggregationResponse aggResp = AggregationResponse.builder()
                .function(agg.getFunction().name())
                .fieldName(agg.getFieldName())
                .filterField(agg.getFilterField())
                .filterValue(agg.getFilterValue())
                .operator(agg.getOperator().name())
                .thresholdValue(agg.getThresholdValue())
                .build();
            builder.aggregation(aggResp);
        }
        
        return builder.build();
    }
    
    public static void updateEntity(Rule rule, RuleUpdateRequest request) {
        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        
        // Update conditions
        if (request.getConditions() != null) {
            List<Condition> conditions = new ArrayList<>();
            for (int i = 0; i < request.getConditions().size(); i++) {
                ConditionRequest condReq = request.getConditions().get(i);
                Condition condition = Condition.builder()
                    .conditionId(UUID.randomUUID().toString())
                    .fieldName(condReq.getFieldName())
                    .operator(Operator.valueOf(condReq.getOperator().toUpperCase()))
                    .comparisonValue(condReq.getComparisonValue())
                    .comparisonValues(condReq.getComparisonValues() != null ? 
                        condReq.getComparisonValues() : new ArrayList<>())
                    .conjunction(condReq.getConjunction())
                    .order(i)
                    .build();
                conditions.add(condition);
            }
            rule.setConditions(conditions);
        }
        
        // Update aggregation
        if (request.getAggregation() != null) {
            AggregationRequest aggReq = request.getAggregation();
            Aggregation aggregation = Aggregation.builder()
                .aggregationId(UUID.randomUUID().toString())
                .function(AggregationFunction.valueOf(aggReq.getFunction().toUpperCase()))
                .fieldName(aggReq.getFieldName())
                .filterField(aggReq.getFilterField())
                .filterValue(aggReq.getFilterValue())
                .operator(Operator.valueOf(aggReq.getOperator().toUpperCase()))
                .thresholdValue(aggReq.getThresholdValue())
                .build();
            rule.setAggregation(aggregation);
        }
        
        rule.setUpdatedAt(LocalDateTime.now());
    }
}