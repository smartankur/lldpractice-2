package carrentalservice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Booking {
    private Vehicle vehicle;
    private PaymentStrategy paymentStrategy;
    private IPaymentDetails paymentDetails;
    private String id;
    private long startTime;
    private long endTime;

    public Boolean conflicts(Booking s2) {
        return s2.getEndTime() <= this.endTime && s2.getStartTime() >= this.startTime;
    }

    // Give method to validate presence of all fields

}
