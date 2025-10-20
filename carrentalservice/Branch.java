package carrentalservice;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Builder
@Data
public class Branch {
    private String name;
    private String city;
    private long latitude;
    private long longitude;
    private Map<VehicleType , Float> perVehiclePrices;
    private Map<VehicleType, List<Vehicle>> vehicles;
}
