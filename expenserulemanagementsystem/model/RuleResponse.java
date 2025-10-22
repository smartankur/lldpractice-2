package expenserulemanagementsystem.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResponse {
    private String ruleId;
    private String ruleName;
    private String description;
    private String ruleType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ConditionResponse> conditions;
    private AggregationResponse aggregation;
}