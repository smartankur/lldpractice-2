package carrentalservice;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class BranchManagerService {
    private final Map<String, Branch> branchNamesToBranches;

    public BranchManagerService(Map<String, Branch> branchNamesToBranches) {
        this.branchNamesToBranches = branchNamesToBranches;
    }
    public boolean addVehicle(String branchName, 
                              Vehicle vehicle) {
        if(!branchNamesToBranches.containsKey(branchName)) {
            return false;
        }
        boolean doesAnyVehicleMatchNumber = branchNamesToBranches.getOrDefault(branchName, null)
                .getVehicles().getOrDefault(vehicle.getType(), Collections.emptyList())
                        .stream()
                                .map(Vehicle::getNumber)
                                        .anyMatch(number -> number.equals(vehicle.getNumber()));
        if(!doesAnyVehicleMatchNumber) {
            branchNamesToBranches.getOrDefault(branchName, null).getVehicles()
                    .computeIfAbsent(vehicle.getType(), p -> new ArrayList<>()).add(vehicle);
        }
        System.out.println("The vehicle has been added");
        return true;
    }

    public boolean allocatePrice(String branchName, 
                                 VehicleType vehicleType, 
                                 float price) {
        branchNamesToBranches.getOrDefault(branchName, null).getPerVehiclePrices().put(vehicleType, price);
        return true;
    }

    public LinkedList<Vehicle> getFreeVehicles(VehicleSearchFilter filter) {
        var branch = branchNamesToBranches.getOrDefault(filter.getBranchName(), null);
        if(branch == null) {
            throw new IllegalArgumentException("Given branch not present");
        }
        TreeMap<Float, List<Vehicle>> vehiclePriceToVehiclePriceWithGivenType = branch.getVehicles().getOrDefault(filter.getVehicleType(),
                Collections.emptyList())
                .stream()
                .filter(vehicle -> isVehicleFree(vehicle, filter.getDesiredBookingSlot()))
                .filter(vehicle -> withinGivenRadius(vehicle, filter))
                .sorted(Comparator.comparing(Vehicle::getRating, Comparator.reverseOrder()))
                .collect(Collectors.groupingBy(vehicle -> branch.getPerVehiclePrices().get(vehicle.getType()),
                        TreeMap::new,
                        Collectors.toList()));
        if(vehiclePriceToVehiclePriceWithGivenType.isEmpty()) {
            throw new IllegalStateException("No FREE vehicle present in this branch with given type");
        }
        var lowestPriceVehiclesAsPerHighestRatingFirst = vehiclePriceToVehiclePriceWithGivenType.firstEntry().getValue();
        return new LinkedList<>(lowestPriceVehiclesAsPerHighestRatingFirst);
    }

    private boolean isVehicleFree(Vehicle vehicle, Booking slot) {
        var bookedBookings = vehicle.getBookings();
        if(bookedBookings.isEmpty()) {
            return true;
        }
        bookedBookings.sort(Comparator.comparing(Booking::getStartTime));
        return bookedBookings
                .stream()
                .anyMatch(s -> !s.conflicts(slot));
    }

    private boolean withinGivenRadius(Vehicle vehicle, VehicleSearchFilter filter) {
        double currentX = filter.getCurrentX();
        double currentY = filter.getCurrentY();

        double vehicleX = vehicle.getCurrentX();
        double vehicleY = vehicle.getCurrentY();

        double distance = Math.sqrt(Math.pow(vehicleX - currentX, 2) + Math.pow(vehicleY - currentY, 2));

        return distance <= filter.getDesiredRadius();
    }
}
