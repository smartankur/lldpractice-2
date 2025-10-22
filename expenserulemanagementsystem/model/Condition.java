package expenserulemanagementsystem.model;

import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Condition {
    private String conditionId;
    private String fieldName;
    private Operator operator;
    private String comparisonValue;
    
    @Builder.Default
    private List<String> comparisonValues = new ArrayList<>();
    
    private String conjunction; // AND/OR
    private int order;
    
    public Condition(String conditionId, String fieldName, Operator operator, int order) {
        this.conditionId = conditionId;
        this.fieldName = fieldName;
        this.operator = operator;
        this.order = order;
        this.comparisonValues = new ArrayList<>();
    }
}
