package expenserulemanagementsystem.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleCreateRequest {
    private String ruleName;
    private String description;
    private String ruleType;
    private List<ConditionRequest> conditions;
    private AggregationRequest aggregation;
}
