package carrentalservice;

import lombok.Getter;

import java.util.*;

@Getter
public class CarRentalServiceController {
   public BranchManagerService branchManagerService;
   public Map<PaymentStrategy, IPaymentService> paymentServices;

    public CarRentalServiceController() {
        this.branchManagerService = new BranchManagerService(Collections.emptyMap());
        paymentServices = new HashMap<>();
        paymentServices.put(PaymentStrategy.CARD, new CardPaymentService());
    }

    public boolean addBranch(Branch branch) {
        this.branchManagerService.getBranchNamesToBranches().putIfAbsent(branch.getName(), branch);
        return true;
    }

    public boolean addVehicle(Vehicle vehicle, String branchName) {
        return branchManagerService.addVehicle(branchName, vehicle);
    }

    public boolean allocatePrice(String branchName,
                                 VehicleType vehicleType,
                                 float price) {
        return branchManagerService.allocatePrice(branchName, vehicleType, price);
    }

    public LinkedList<Vehicle> searchVehicles(VehicleSearchFilter vehicleSearchFilter) {
        return this.branchManagerService.getFreeVehicles(vehicleSearchFilter);
    }

    public boolean bookVehicle(Booking booking) {
        synchronized (this) {
            var vehicle = booking.getVehicle();
            if(!vehicle.getStatus().equals(Status.BOOKED)) {
                vehicle.setStatus(Status.BOOKED);
                vehicle.getBookings().add(booking);
                paymentServices.get(booking.getPaymentStrategy()).paymentDetails(booking.getPaymentDetails())
                        .performPayment();
            }

            // add exception handling here for failure scenarios
            return true;
        }
    }
}
