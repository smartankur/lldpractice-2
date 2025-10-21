package orderdeliveryservice.repository;

import java.math.BigDecimal;

/**
 * Repository interface for Payment tracking operations.
 */
public interface IPaymentRepository {
    
    /**
     * Records a delivery cost.
     * 
     * @param cost the cost to add to total
     */
    void recordCost(BigDecimal cost);
    
    /**
     * Records a payment settlement.
     * 
     * @param paidAmount the amount paid
     */
    void recordPayment(BigDecimal paidAmount);
    
    /**
     * Gets the total cost of all deliveries.
     * 
     * @return total cost
     */
    BigDecimal getTotalCost();
    
    /**
     * Gets the total paid amount.
     * 
     * @return total paid
     */
    BigDecimal getTotalPaid();
    
    /**
     * Gets the unpaid cost (total - paid).
     * 
     * @return unpaid cost
     */
    BigDecimal getUnpaidCost();
}