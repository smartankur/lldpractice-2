package carrentalservice;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Vehicle {
    private String number;
    private VehicleType type;
    private int rating;
    private Status status;

    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    private long currentX;
    private long currentY;

    public void setRating(int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating should be between 1 and 5");
        }
        this.rating = rating;
    }

    public void setStatus(Status status) {
        if (this.status.equals(Status.IN_TRANSIT) && status.equals(Status.BOOKED)) {
            throw new IllegalArgumentException("IN_TRANSIT vehicle can't be booked");
        }
        this.status = status;
    }
}
