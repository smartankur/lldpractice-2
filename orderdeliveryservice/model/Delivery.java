package orderdeliveryservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@Getter
public class Delivery {
    private final String driverId;
    private final Long startTime;
    private final Long endTime;

    @Setter
    private DeliveryStatus status;

    @Setter
    private BigDecimal cost;

    /**
     * Returns duration in seconds
     */
    public long getDurationInSeconds() {
        return endTime - startTime;
    }
}