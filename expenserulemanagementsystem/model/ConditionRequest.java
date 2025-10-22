package expenserulemanagementsystem.model;

import lombok.*;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionRequest {
    private String fieldName;
    private String operator;
    private String comparisonValue;
    private List<String> comparisonValues;
    private String conjunction;
}