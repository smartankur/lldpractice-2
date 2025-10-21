package orderdeliveryservice.mapper;

import orderdeliveryservice.model.Driver;
import orderdeliveryservice.model.Rate;

public class DriverMapper {

    public Driver getDriverDto(String driverId, Rate rate) {
        return Driver.builder()
                .driverId(driverId)
                .rate(rate)
                .build();
    }
}
