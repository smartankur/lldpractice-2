package expenserulemanagementsystem.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aggregation {
    private String aggregationId;
    private AggregationFunction function;
    private String fieldName;
    private String filterField;
    private String filterValue;
    private Operator operator;
    private String thresholdValue;
    
    public Aggregation(String aggregationId, AggregationFunction function, String fieldName) {
        this.aggregationId = aggregationId;
        this.function = function;
        this.fieldName = fieldName;
    }
}