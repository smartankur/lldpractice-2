package carrentalservice;

// ============== Common Import Statements ==============
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// ============== Model Classes ==============

@Data
@Builder
private class Vehicle {
    private String id;
    private VehicleType type;
    private String branchName;
    private Status status;
    private double rating; // For bonus feature
    @Builder.Default
    private List<Booking> bookings = new CopyOnWriteArrayList<>(); // Thread-safe list

    public enum Status {
        AVAILABLE,
        BOOKED,
        MAINTENANCE
    }

    // Thread-safe method to check availability
    public synchronized boolean isAvailableForSlot(long startTime, long endTime) {
        if (status == Status.MAINTENANCE) return false;

        return bookings.stream()
                .noneMatch(booking -> booking.conflictsWith(startTime, endTime));
    }

    // Thread-safe method to add booking
    public synchronized boolean addBooking(Booking booking) {
        if (!isAvailableForSlot(booking.getStartTime(), booking.getEndTime())) {
            return false;
        }
        bookings.add(booking);
        return true;
    }
}

@Data
@Builder
public class Booking {
    private String id;
    private Vehicle vehicle;
    private long startTime;
    private long endTime;
    private PaymentStrategy paymentStrategy;
    private IPaymentDetails paymentDetails;
    private BookingStatus status;
    private double totalAmount;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }

    public boolean conflictsWith(long otherStart, long otherEnd) {
        // Check if time slots overlap
        return !(endTime <= otherStart || startTime >= otherEnd);
    }

    public boolean conflictsWith(Booking other) {
        return conflictsWith(other.startTime, other.endTime);
    }

    // Validate booking times
    public void validate() {
        if (startTime >= endTime) {
            throw new InvalidBookingException("Start time must be before end time");
        }
        if (startTime < System.currentTimeMillis()) {
            throw new InvalidBookingException("Cannot book in the past");
        }
        // Check hourly granularity
        if (startTime % 3600000 != 0 || endTime % 3600000 != 0) {
            throw new InvalidBookingException("Booking times must be in hourly slots");
        }
    }
}

@Data
@Builder
public class Branch {
    private String name;
    private String city;
    private double latitude;
    private double longitude;
    @Builder.Default
    private Map<VehicleType, Float> perVehiclePrices = new ConcurrentHashMap<>();
    @Builder.Default
    private Map<VehicleType, List<Vehicle>> vehicles = new ConcurrentHashMap<>();

    public synchronized boolean addVehicle(Vehicle vehicle) {
        vehicles.computeIfAbsent(vehicle.getType(), k -> new ArrayList<>()).add(vehicle);
        return true;
    }

    public List<Vehicle> getAvailableVehicles(VehicleType type, long startTime, long endTime) {
        return vehicles.getOrDefault(type, Collections.emptyList()).stream()
                .filter(v -> v.isAvailableForSlot(startTime, endTime))
                .collect(Collectors.toList());
    }

    public float getPriceForType(VehicleType type) {
        return perVehiclePrices.getOrDefault(type, Float.MAX_VALUE);
    }
}

@Data
@Builder
public class VehicleSearchFilter {
    private VehicleType vehicleType;
    private long startTime;
    private long endTime;
    private String branchName;
    private Double currentX;
    private Double currentY;
    private Long desiredRadius; // in meters
    private Float priceUpperBound;
    private Double minRating; // For bonus feature
    private Booking desiredBookingSlot;

    public void setDesiredBookingSlot(LocalDateTime startTime, LocalDateTime endTime) {
        this.desiredBookingSlot = Booking.builder()
                .startTime(startTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000)
                .endTime(endTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000)
                .build();
    }
}

// ============== Strategy Pattern for Booking ==============

public interface BookingStrategy {
    Vehicle selectVehicle(List<VehicleWithPrice> availableVehicles, VehicleSearchFilter filter);

    class VehicleWithPrice {
        public final Vehicle vehicle;
        public final Branch branch;
        public final float pricePerHour;

        public VehicleWithPrice(Vehicle vehicle, Branch branch, float pricePerHour) {
            this.vehicle = vehicle;
            this.branch = branch;
            this.pricePerHour = pricePerHour;
        }
    }
}

public class LowestPriceStrategy implements BookingStrategy {
    @Override
    public Vehicle selectVehicle(List<VehicleWithPrice> availableVehicles, VehicleSearchFilter filter) {
        return availableVehicles.stream()
                .min(Comparator.comparing(vwp -> vwp.pricePerHour))
                .map(vwp -> vwp.vehicle)
                .orElse(null);
    }
}

public class HighestRatingStrategy implements BookingStrategy {
    @Override
    public Vehicle selectVehicle(List<VehicleWithPrice> availableVehicles, VehicleSearchFilter filter) {
        return availableVehicles.stream()
                .max(Comparator.comparing(vwp -> vwp.vehicle.getRating()))
                .map(vwp -> vwp.vehicle)
                .orElse(null);
    }
}

public class NearestLocationStrategy implements BookingStrategy {
    @Override
    public Vehicle selectVehicle(List<VehicleWithPrice> availableVehicles, VehicleSearchFilter filter) {
        if (filter.getCurrentX() == null || filter.getCurrentY() == null) {
            return new LowestPriceStrategy().selectVehicle(availableVehicles, filter);
        }

        return availableVehicles.stream()
                .min(Comparator.comparing(vwp -> calculateDistance(
                        filter.getCurrentX(), filter.getCurrentY(),
                        vwp.branch.getLatitude(), vwp.branch.getLongitude()
                )))
                .map(vwp -> vwp.vehicle)
                .orElse(null);
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}

// ============== Payment System ==============

public interface IPaymentDetails {
    String getPaymentMethod();
    boolean validate();
}

public interface IPaymentService {
    IPaymentService paymentDetails(IPaymentDetails paymentDetails);
    boolean performPayment(double amount);
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardPaymentDetails implements IPaymentDetails {
    private String name;
    private String cardNumber;
    private String cvv;
    private String expiryDate;

    @Override
    public String getPaymentMethod() {
        return "CARD";
    }

    @Override
    public boolean validate() {
        return cardNumber != null && cardNumber.length() == 16
                && cvv != null && cvv.length() == 3;
    }
}

public class CardPaymentService implements IPaymentService {
    private CardPaymentDetails cardPaymentDetails;

    @Override
    public IPaymentService paymentDetails(IPaymentDetails paymentDetails) {
        if (!(paymentDetails instanceof CardPaymentDetails)) {
            throw new IllegalArgumentException("Invalid payment details type");
        }
        this.cardPaymentDetails = (CardPaymentDetails) paymentDetails;
        return this;
    }

    @Override
    public boolean performPayment(double amount) {
        if (cardPaymentDetails == null || !cardPaymentDetails.validate()) {
            return false;
        }
        // Simulate payment processing
        System.out.println("Processing card payment of " + amount);
        return true;
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UPIPaymentDetails implements IPaymentDetails {
    private String upiId;
    private String pin;

    @Override
    public String getPaymentMethod() {
        return "UPI";
    }

    @Override
    public boolean validate() {
        return upiId != null && upiId.contains("@") && pin != null && pin.length() >= 4;
    }
}

public class UPIPaymentService implements IPaymentService {
    private UPIPaymentDetails upiPaymentDetails;

    @Override
    public IPaymentService paymentDetails(IPaymentDetails paymentDetails) {
        if (!(paymentDetails instanceof UPIPaymentDetails)) {
            throw new IllegalArgumentException("Invalid payment details type");
        }
        this.upiPaymentDetails = (UPIPaymentDetails) paymentDetails;
        return this;
    }

    @Override
    public boolean performPayment(double amount) {
        if (upiPaymentDetails == null || !upiPaymentDetails.validate()) {
            return false;
        }
        // Simulate payment processing
        System.out.println("Processing UPI payment of " + amount);
        return true;
    }
}

// ============== Enums ==============

public enum PaymentStrategy {
    CARD,
    UPI
}

public enum VehicleType {
    SEDAN,
    HATCHBACK,
    SUV
}

// ============== Service Classes ==============

public class BranchManagerService {
    private final Map<String, Branch> branchNamesToBranches;

    public BranchManagerService() {
        this.branchNamesToBranches = new ConcurrentHashMap<>();
    }

    public boolean addBranch(String branchName, String city, double latitude, double longitude) {
        if (branchNamesToBranches.containsKey(branchName)) {
            throw new BranchAlreadyExistsException("Branch " + branchName + " already exists");
        }

        Branch branch = Branch.builder()
                .name(branchName)
                .city(city)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        branchNamesToBranches.put(branchName, branch);
        return true;
    }

    public boolean addVehicle(String vehicleId, VehicleType vehicleType, String branchName) {
        Branch branch = branchNamesToBranches.get(branchName);
        if (branch == null) {
            throw new BranchNotFoundException("Branch " + branchName + " not found");
        }

        // Check for duplicate vehicle ID
        boolean vehicleExists = branchNamesToBranches.values().stream()
                .flatMap(b -> b.getVehicles().values().stream())
                .flatMap(List::stream)
                .anyMatch(v -> v.getId().equals(vehicleId));

        if (vehicleExists) {
            throw new VehicleAlreadyExistsException("Vehicle " + vehicleId + " already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .id(vehicleId)
                .type(vehicleType)
                .branchName(branchName)
                .status(Vehicle.Status.AVAILABLE)
                .rating(4.5) // Default rating
                .build();

        return branch.addVehicle(vehicle);
    }

    public boolean allocatePrice(String branchName, VehicleType vehicleType, float price) {
        Branch branch = branchNamesToBranches.get(branchName);
        if (branch == null) {
            throw new BranchNotFoundException("Branch " + branchName + " not found");
        }

        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        branch.getPerVehiclePrices().put(vehicleType, price);
        return true;
    }

    public List<Vehicle> getFreeVehicles(VehicleSearchFilter filter) {
        List<BookingStrategy.VehicleWithPrice> vehiclesWithPrices = new ArrayList<>();

        for (Branch branch : branchNamesToBranches.values()) {
            // Apply branch filter if specified
            if (filter.getBranchName() != null && !branch.getName().equals(filter.getBranchName())) {
                continue;
            }

            // Apply radius filter if specified
            if (filter.getDesiredRadius() != null && filter.getCurrentX() != null && filter.getCurrentY() != null) {
                double distance = calculateDistance(
                        filter.getCurrentX(), filter.getCurrentY(),
                        branch.getLatitude(), branch.getLongitude()
                );
                if (distance > filter.getDesiredRadius()) {
                    continue;
                }
            }

            List<Vehicle> availableVehicles = branch.getAvailableVehicles(
                    filter.getVehicleType(),
                    filter.getStartTime(),
                    filter.getEndTime()
            );

            for (Vehicle vehicle : availableVehicles) {
                // Apply rating filter if specified
                if (filter.getMinRating() != null && vehicle.getRating() < filter.getMinRating()) {
                    continue;
                }

                float price = branch.getPriceForType(vehicle.getType());

                // Apply price filter if specified
                if (filter.getPriceUpperBound() != null && price > filter.getPriceUpperBound()) {
                    continue;
                }

                vehiclesWithPrices.add(new BookingStrategy.VehicleWithPrice(vehicle, branch, price));
            }
        }

        return vehiclesWithPrices.stream()
                .sorted(Comparator.comparing(vwp -> vwp.pricePerHour))
                .map(vwp -> vwp.vehicle)
                .collect(Collectors.toList());
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public Map<String, Map<VehicleType, List<VehicleInventoryItem>>> viewInventory(long startTime, long endTime) {
        Map<String, Map<VehicleType, List<VehicleInventoryItem>>> inventory = new HashMap<>();

        for (Branch branch : branchNamesToBranches.values()) {
            Map<VehicleType, List<VehicleInventoryItem>> branchInventory = new HashMap<>();

            for (Map.Entry<VehicleType, List<Vehicle>> entry : branch.getVehicles().entrySet()) {
                List<VehicleInventoryItem> items = entry.getValue().stream()
                        .map(v -> new VehicleInventoryItem(
                                v.getId(),
                                v.getType(),
                                v.isAvailableForSlot(startTime, endTime) ? "Available" : "Booked"
                        ))
                        .collect(Collectors.toList());

                if (!items.isEmpty()) {
                    branchInventory.put(entry.getKey(), items);
                }
            }

            if (!branchInventory.isEmpty()) {
                inventory.put(branch.getName(), branchInventory);
            }
        }

        return inventory;
    }

    public static class VehicleInventoryItem {
        public final String vehicleId;
        public final VehicleType type;
        public final String status;

        public VehicleInventoryItem(String vehicleId, VehicleType type, String status) {
            this.vehicleId = vehicleId;
            this.type = type;
            this.status = status;
        }

        @Override
        public String toString() {
            return type + " " + vehicleId + " " + status;
        }
    }

    public Branch getBranch(String branchName) {
        return branchNamesToBranches.get(branchName);
    }
}

// ============== Main Controller ==============

public class CarRentalServiceController {
    private final BranchManagerService branchManagerService;
    private final Map<PaymentStrategy, IPaymentService> paymentServices;
    private final BookingStrategy bookingStrategy;
    private final AtomicLong  bookingIdGenerator;
    private final Map<String, Booking> allBookings;

    public CarRentalServiceController(BookingStrategy bookingStrategy) {
        this.branchManagerService = new BranchManagerService();
        this.paymentServices = new ConcurrentHashMap<>();
        this.bookingStrategy = bookingStrategy != null ? bookingStrategy : new LowestPriceStrategy();
        this.bookingIdGenerator = new AtomicLong(1);
        this.allBookings = new ConcurrentHashMap<>();

        // Initialize payment services
        paymentServices.put(PaymentStrategy.CARD, new CardPaymentService());
        paymentServices.put(PaymentStrategy.UPI, new UPIPaymentService());
    }

    public CarRentalServiceController() {
        this(new LowestPriceStrategy());
    }

    public boolean addBranch(String branchName) {
        // Default city Delhi, with default coordinates
        return branchManagerService.addBranch(branchName, "Delhi", 28.6139, 77.2090);
    }

    public boolean addVehicle(String vehicleId, VehicleType vehicleType, String branchName) {
        return branchManagerService.addVehicle(vehicleId, vehicleType, branchName);
    }

    public boolean allocatePrice(String branchName, VehicleType vehicleType, float price) {
        return branchManagerService.allocatePrice(branchName, vehicleType, price);
    }

    public LinkedList<Vehicle> searchVehicles(VehicleSearchFilter filter) {
        return new LinkedList<>(branchManagerService.getFreeVehicles(filter));
    }

    public String bookVehicle(VehicleType vehicleType, long startTime, long endTime) {
        return bookVehicle(vehicleType, startTime, endTime, null, null);
    }

    public String bookVehicle(VehicleType vehicleType, long startTime, long endTime,
                              PaymentStrategy paymentStrategy, IPaymentDetails paymentDetails) {
        // Create filter for search
        VehicleSearchFilter filter = VehicleSearchFilter.builder()
                .vehicleType(vehicleType)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        List<Vehicle> availableVehicles = branchManagerService.getFreeVehicles(filter);

        if (availableVehicles.isEmpty()) {
            return "NO " + vehicleType + " AVAILABLE";
        }

        // Select vehicle based on strategy
        Vehicle selectedVehicle = availableVehicles.get(0); // Already sorted by lowest price

        // Create booking
        String bookingId = "BK" + bookingIdGenerator.getAndIncrement();
        Booking booking = Booking.builder()
                .id(bookingId)
                .vehicle(selectedVehicle)
                .startTime(startTime)
                .endTime(endTime)
                .paymentStrategy(paymentStrategy != null ? paymentStrategy : PaymentStrategy.CARD)
                .paymentDetails(paymentDetails)
                .status(Booking.BookingStatus.PENDING)
                .build();

        // Validate booking
        try {
            booking.validate();
        } catch (InvalidBookingException e) {
            return "Invalid booking: " + e.getMessage();
        }

        // Try to add booking to vehicle (thread-safe)
        if (!selectedVehicle.addBooking(booking)) {
            return "Vehicle no longer available for the selected time slot";
        }

        // Calculate total amount
        Branch branch = branchManagerService.getBranch(selectedVehicle.getBranchName());
        float pricePerHour = branch.getPriceForType(vehicleType);
        long hours = (endTime - startTime) / 3600000; // Convert ms to hours
        double totalAmount = pricePerHour * hours;
        booking.setTotalAmount(totalAmount);

        // Process payment if details provided
        if (paymentDetails != null && paymentStrategy != null) {
            IPaymentService paymentService = paymentServices.get(paymentStrategy);
            if (paymentService != null) {
                boolean paymentSuccess = paymentService
                        .paymentDetails(paymentDetails)
                        .performPayment(totalAmount);

                if (paymentSuccess) {
                    booking.setStatus(Booking.BookingStatus.CONFIRMED);
                } else {
                    // Remove booking if payment failed
                    selectedVehicle.getBookings().remove(booking);
                    return "Payment failed for booking";
                }
            }
        } else {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
        }

        allBookings.put(bookingId, booking);

        return "\"" + selectedVehicle.getId() + "\" from " + selectedVehicle.getBranchName() +
                " booked. Booking ID: " + bookingId + ". Total Amount: Rs " + totalAmount;
    }

    public void viewInventory(long startTime, long endTime) {
        Map<String, Map<VehicleType, List<BranchManagerService.VehicleInventoryItem>>> inventory =
                branchManagerService.viewInventory(startTime, endTime);

        for (Map.Entry<String, Map<VehicleType, List<BranchManagerService.VehicleInventoryItem>>> branchEntry : inventory.entrySet()) {
            System.out.println("Branch: " + branchEntry.getKey());

            for (Map.Entry<VehicleType, List<BranchManagerService.VehicleInventoryItem>> typeEntry : branchEntry.getValue().entrySet()) {
                for (BranchManagerService.VehicleInventoryItem item : typeEntry.getValue()) {
                    System.out.println("  " + item);
                }
            }
        }
    }

    // Bonus: Advanced booking with filters
    public String bookVehicleWithFilters(VehicleSearchFilter filter,
                                         PaymentStrategy paymentStrategy,
                                         IPaymentDetails paymentDetails) {
        List<Vehicle> availableVehicles = branchManagerService.getFreeVehicles(filter);

        if (availableVehicles.isEmpty()) {
            return "No vehicles available matching the criteria";
        }

        Vehicle selectedVehicle = availableVehicles.get(0);

        // Rest of the booking logic similar to bookVehicle method
        return bookVehicle(selectedVehicle.getType(), filter.getStartTime(), filter.getEndTime(),
                paymentStrategy, paymentDetails);
    }
}

// ============== Custom Exceptions ==============

public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(String message) {
        super(message);
    }
}

public class BranchAlreadyExistsException extends RuntimeException {
    public BranchAlreadyExistsException(String message) {
        super(message);
    }
}

public class VehicleAlreadyExistsException extends RuntimeException {
    public VehicleAlreadyExistsException(String message) {
        super(message);
    }
}

// ============== Bonus: Filter Combinator ==============

public class FilterCombinator {

    public enum Operator {
        AND, OR
    }

    public static class FilterNode {
        private final Operator operator;
        private final List<FilterNode> children;
        private final Predicate<Vehicle> predicate;

        // For leaf nodes
        public FilterNode(Predicate<Vehicle> predicate) {
            this.predicate = predicate;
            this.operator = null;
            this.children = null;
        }

        // For operator nodes
        public FilterNode(Operator operator, FilterNode... children) {
            this.operator = operator;
            this.children = List.of(children);
            this.predicate = null;
        }

        public boolean evaluate(Vehicle vehicle) {
            if (predicate != null) {
                return predicate.test(vehicle);
            }

            if (operator == Operator.AND) {
                return children.stream().allMatch(child -> child.evaluate(vehicle));
            } else if (operator == Operator.OR) {
                return children.stream().anyMatch(child -> child.evaluate(vehicle));
            }

            return false;
        }
    }

    // Example usage for complex filter:
    // (vehicle is sedan AND slot is available AND (rating > 4 OR radius is within 1 km))
    public static FilterNode createComplexFilter(VehicleSearchFilter baseFilter) {
        Predicate<Vehicle> isSedan = v -> v.getType() == VehicleType.SEDAN;
        Predicate<Vehicle> isAvailable = v -> v.isAvailableForSlot(
                baseFilter.getStartTime(), baseFilter.getEndTime());
        Predicate<Vehicle> highRating = v -> v.getRating() > 4.0;
        Predicate<Vehicle> nearbyLocation = v -> {
            // This would need branch location data
            return true; // Simplified for example
        };

        FilterNode sedanNode = new FilterNode(isSedan);
        FilterNode availableNode = new FilterNode(isAvailable);
        FilterNode ratingNode = new FilterNode(highRating);
        FilterNode locationNode = new FilterNode(nearbyLocation);

        FilterNode ratingOrLocation = new FilterNode(Operator.OR, ratingNode, locationNode);
        FilterNode finalFilter = new FilterNode(Operator.AND, sedanNode, availableNode, ratingOrLocation);

        return finalFilter;
    }
}

// ============== Main Application ==============

public class Main {
    public static void main(String[] args) {
        CarRentalServiceController controller = new CarRentalServiceController();

        // Test scenario from the problem statement
        controller.addBranch("Vasanth Vihar");
        controller.addBranch("Cyber City");

        controller.allocatePrice("Vasanth Vihar", VehicleType.SEDAN, 100);
        controller.allocatePrice("Vasanth Vihar", VehicleType.HATCHBACK, 80);
        controller.allocatePrice("Cyber City", VehicleType.SEDAN, 200);
        controller.allocatePrice("Cyber City", VehicleType.HATCHBACK, 50);

        controller.addVehicle("DL 01 MR 9310", VehicleType.SEDAN, "Vasanth Vihar");
        controller.addVehicle("DL 01 MR 9311", VehicleType.SEDAN, "Cyber City");
        controller.addVehicle("DL 01 MR 9312", VehicleType.HATCHBACK, "Cyber City");

        // Create time slots
        LocalDateTime feb29_10am = LocalDateTime.of(2020, Month.FEBRUARY, 29, 10, 0);
        LocalDateTime feb29_1pm = LocalDateTime.of(2020, Month.FEBRUARY, 29, 13, 0);
        LocalDateTime feb29_2pm = LocalDateTime.of(2020, Month.FEBRUARY, 29, 14, 0);
        LocalDateTime feb29_3pm = LocalDateTime.of(2020, Month.FEBRUARY, 29, 15, 0);
        LocalDateTime feb29_4pm = LocalDateTime.of(2020, Month.FEBRUARY, 29, 16, 0);
        LocalDateTime feb29_5pm = LocalDateTime.of(2020, Month.FEBRUARY, 29, 17, 0);

        long time_10am = feb29_10am.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long time_1pm = feb29_1pm.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long time_2pm = feb29_2pm.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long time_3pm = feb29_3pm.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long time_4pm = feb29_4pm.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;
        long time_5pm = feb29_5pm.toEpochSecond(java.time.ZoneOffset.UTC) * 1000;

        System.out.println("Booking 1: " + controller.bookVehicle(VehicleType.SEDAN, time_10am, time_1pm));
        System.out.println("Booking 2: " + controller.bookVehicle(VehicleType.SEDAN, time_2pm, time_3pm));
        System.out.println("Booking 3: " + controller.bookVehicle(VehicleType.SEDAN, time_2pm, time_3pm));
        System.out.println("Booking 4: " + controller.bookVehicle(VehicleType.SEDAN, time_2pm, time_3pm));

        System.out.println("\nInventory 2-3 PM:");
        controller.viewInventory(time_2pm, time_3pm);

        System.out.println("\nInventory 4-5 PM:");
        controller.viewInventory(time_4pm, time_5pm);

        // Bonus: Test advanced booking with filters
        System.out.println("\n=== Testing Advanced Booking with Filters ===");

        // Add more vehicles for testing
        controller.addVehicle("DL 01 MR 9313", VehicleType.SUV, "Vasanth Vihar");
        controller.allocatePrice("Vasanth Vihar", VehicleType.SUV, 300);

        // Test with rating filter
        VehicleSearchFilter ratingFilter = VehicleSearchFilter.builder()
                .vehicleType(VehicleType.SEDAN)
                .startTime(time_4pm)
                .endTime(time_5pm)
                .minRating(4.0)
                .build();

        System.out.println("Booking with rating filter: " +
                controller.bookVehicleWithFilters(ratingFilter, PaymentStrategy.CARD,
                        new CardPaymentDetails("John Doe", "1234567890123456", "123", "12/25")));

        // Test with location radius filter
        VehicleSearchFilter locationFilter = VehicleSearchFilter.builder()
                .vehicleType(VehicleType.HATCHBACK)
                .startTime(time_4pm)
                .endTime(time_5pm)
                .currentX(28.6)
                .currentY(77.2)
                .desiredRadius(10000L) // 10km radius
                .build();

        System.out.println("Booking with location filter: " +
                controller.bookVehicle(VehicleType.HATCHBACK, time_4pm, time_5pm));

        // Test with price filter
        VehicleSearchFilter priceFilter = VehicleSearchFilter.builder()
                .vehicleType(VehicleType.HATCHBACK)
                .startTime(time_10am)
                .endTime(time_1pm)
                .priceUpperBound(100.0f)
                .build();

        System.out.println("Booking with price filter (max 100/hr): " +
                controller.bookVehicle(VehicleType.HATCHBACK, time_10am, time_1pm));
    }
}*/