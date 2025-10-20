package carrentalservice;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Getter
@Builder
public class VehicleSearchFilter {
    private VehicleType vehicleType;
    private long desiredRadius;
    private long currentX;
    private String branchName;
    private long currentY;
    private Float priceUpperBound;
    private Booking desiredBookingSlot;

    public void setDesiredBookingSlot(LocalDateTime startTime, LocalDateTime endTime) {
        this.desiredBookingSlot = Booking.builder()
                .startTime(startTime.toEpochSecond(ZoneOffset.UTC))
                .endTime(endTime.toEpochSecond(ZoneOffset.UTC)).build();
    }
}
