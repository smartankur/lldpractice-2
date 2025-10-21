package orderdeliveryservice.service;

import orderdeliveryservice.model.Delivery;
import orderdeliveryservice.model.DeliveryStatus;
import orderdeliveryservice.model.Driver;
import orderdeliveryservice.model.Rate;
import orderdeliveryservice.repository.IDeliveryRepository;
import orderdeliveryservice.repository.IDriverRepository;
import orderdeliveryservice.repository.IPaymentRepository;
import orderdeliveryservice.service.cost.CostCalculator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main service managing drivers, deliveries, and payments using Repository pattern.
 *
 * Design decisions:
 * - Repository pattern for separation of concerns and testability
 * - Dependency injection for repositories and cost calculator
 * - Two-pointer approach in repository for O(1) amortized payment processing
 * - Time format: Unix epoch seconds (Long) for 1-second precision
 */
public class DeliveryPaymentSystem {

    private static final long MAX_DELIVERY_DURATION_SECONDS = 10800; // 3 hours

    private final IDriverRepository driverRepository;
    private final IDeliveryRepository deliveryRepository;
    private final IPaymentRepository paymentRepository;
    private final CostCalculator costCalculator;

    /**
     * Constructor with dependency injection.
     *
     * @param driverRepository repository for driver operations
     * @param deliveryRepository repository for delivery operations
     * @param paymentRepository repository for payment tracking
     * @param costCalculator strategy for cost calculation
     */
    public DeliveryPaymentSystem(
            IDriverRepository driverRepository,
            IDeliveryRepository deliveryRepository,
            IPaymentRepository paymentRepository,
            CostCalculator costCalculator) {
        this.driverRepository = driverRepository;
        this.deliveryRepository = deliveryRepository;
        this.paymentRepository = paymentRepository;
        this.costCalculator = costCalculator;
    }

    /**
     * Registers a new driver with their hourly rate.
     *
     * @param driverId unique identifier for the driver
     * @param rate the payment rate configuration
     * @throws IllegalArgumentException if driver already exists or invalid input
     */
    public void addDriver(String driverId, Rate rate) {
        validateDriverInput(driverId, rate);

        Driver driver = Driver.builder()
                .driverId(driverId)
                .rate(rate)
                .build();

        driverRepository.save(driver); // Will throw if duplicate
    }

    /**
     * Records a completed delivery.
     *
     * Time format: Unix epoch seconds (Long)
     * - Provides 1-second precision as required
     * - Easy arithmetic and comparisons
     * - No timezone issues
     *
     * @param driverId the driver who completed the delivery
     * @param startTime delivery start time in epoch seconds
     * @param endTime delivery end time in epoch seconds
     * @throws IllegalArgumentException for invalid inputs
     */
    public void recordDelivery(String driverId, Long startTime, Long endTime) {
        // Validation
        validateDeliveryInput(driverId, startTime, endTime);

        Driver driver = driverRepository.findById(driverId);
        if (driver == null) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }

        // Create delivery object
        Delivery delivery = Delivery.builder()
                .driverId(driverId)
                .startTime(startTime)
                .endTime(endTime)
                .status(DeliveryStatus.UNPAID)
                .build();

        // ✅ FIXED: Calculate and SET cost before saving (avoids null cost bug)
        BigDecimal cost = costCalculator.calculateCost(driver, delivery);
        delivery.setCost(cost);

        // Save to repositories
        deliveryRepository.save(delivery);
        paymentRepository.recordCost(cost);
    }

    /**
     * Returns the total cost of all deliveries (paid + unpaid).
     *
     * Time complexity: O(1) - precomputed in payment repository
     *
     * @return total cost as BigDecimal
     */
    public BigDecimal getTotalCost() {
        return paymentRepository.getTotalCost();
    }

    /**
     * Settles payment for all deliveries completed up to and including the given time.
     *
     * Uses two-pointer approach in repository:
     * - Each delivery is processed exactly once
     * - Amortized O(1) per delivery across all payUpTo calls
     * - Works because deliveries arrive in chronological order
     *
     * @param payTime timestamp in epoch seconds
     */
    public void payUpTo(Long payTime) {
        if (payTime == null) {
            throw new IllegalArgumentException("Pay time cannot be null");
        }

        // Get deliveries to pay (without marking them as paid yet)
        List<Delivery> deliveriesToPay = deliveryRepository.findUnpaidDeliveriesUpTo(payTime);

        // Calculate total payment amount
        BigDecimal paymentAmount = BigDecimal.ZERO;
        for (Delivery delivery : deliveriesToPay) {
            // ✅ FIXED: Properly accumulate (BigDecimal is immutable!)
            paymentAmount = paymentAmount.add(delivery.getCost());
        }

        // Mark deliveries as paid in repository
        deliveryRepository.markAsPaidUpTo(payTime);

        // Record payment
        paymentRepository.recordPayment(paymentAmount);
    }

    /**
     * Returns the total cost of unpaid deliveries.
     *
     * Time complexity: O(1) - simple subtraction in payment repository
     *
     * @return unpaid cost as BigDecimal
     */
    public BigDecimal getTotalCostUnpaid() {
        return paymentRepository.getUnpaidCost();
    }

    /**
     * Get all deliveries (for analysis/debugging).
     *
     * @return list of all deliveries
     */
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    /**
     * Get driver by ID (for testing/debugging).
     */
    public Driver getDriver(String driverId) {
        return driverRepository.findById(driverId);
    }

    /**
     * Get total number of deliveries recorded.
     */
    public int getTotalDeliveriesCount() {
        return deliveryRepository.getTotalCount();
    }

    /**
     * Get number of paid deliveries.
     */
    public int getPaidDeliveriesCount() {
        return deliveryRepository.getPaidCount();
    }

    /**
     * Get number of unpaid deliveries.
     */
    public int getUnpaidDeliveriesCount() {
        return deliveryRepository.getUnpaidCount();
    }

    // ========== Private Validation Methods ==========

    private void validateDriverInput(String driverId, Rate rate) {
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty");
        }
        if (rate == null || rate.getRate() == null || rate.getWindow() == null) {
            throw new IllegalArgumentException("Rate configuration is invalid");
        }
        if (rate.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be positive");
        }
    }

    private void validateDeliveryInput(String driverId, Long startTime, Long endTime) {
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Times cannot be null");
        }
        if (startTime >= endTime) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (endTime - startTime > MAX_DELIVERY_DURATION_SECONDS) {
            throw new IllegalArgumentException("Delivery cannot exceed 3 hours");
        }
    }
}