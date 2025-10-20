package loggersystem;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ConsoleSinkDetails extends SinkDetails {
    // Console sink might not need additional fields
    
    @Override
    public void validate() {
        // No specific validation needed for console
    }
}