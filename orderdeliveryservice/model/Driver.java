package orderdeliveryservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class Driver {
    private final String driverId;
    private final Rate rate;
}
