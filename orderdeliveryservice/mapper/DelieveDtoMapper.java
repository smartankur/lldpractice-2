package orderdeliveryservice.mapper;

import orderdeliveryservice.model.Delivery;
import orderdeliveryservice.model.DeliveryStatus;

import java.util.UUID;

public class DelieveDtoMapper {


    public Delivery getDeliveryDto(Long startTime, Long endTime) {
        return Delivery.builder()
                .startTime(startTime)
                .endTime(endTime)
                .status(DeliveryStatus.UNPAID)
                .build();
    }
}
