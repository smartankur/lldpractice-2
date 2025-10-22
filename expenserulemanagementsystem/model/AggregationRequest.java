package expenserulemanagementsystem.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AggregationRequest {
    private String function;
    private String fieldName;
    private String filterField;
    private String filterValue;
    private String operator;
    private String thresholdValue;
}