package expenserulemanagementsystem.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {
    private String ruleId;
    private String ruleName;
    private String description;
    private RuleType ruleType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<Condition> conditions = new ArrayList<>();
    
    private Aggregation aggregation; // for trip-level rules
    
    public Rule(String ruleId, String ruleName, String description, RuleType ruleType) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.description = description;
        this.ruleType = ruleType;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.conditions = new ArrayList<>();
    }
}