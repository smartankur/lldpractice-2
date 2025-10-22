package expenserulemanagementsystem.model;

import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleUpdateRequest {
    private String ruleName;
    private String description;
    private List<ConditionRequest> conditions;
    private AggregationRequest aggregation;
}