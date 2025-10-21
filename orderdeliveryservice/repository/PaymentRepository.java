package orderdeliveryservice.repository;

import java.math.BigDecimal;

/**
 * In-memory implementation of Payment repository.
 * Tracks total costs and payments for O(1) cost retrieval.
 */
public class PaymentRepository implements IPaymentRepository {

    private BigDecimal totalCost;
    private BigDecimal totalPaid;

    public PaymentRepository() {
        this.totalCost = BigDecimal.ZERO;
        this.totalPaid = BigDecimal.ZERO;
    }

    @Override
    public void recordCost(BigDecimal cost) {
        if (cost == null) {
            throw new IllegalArgumentException("Cost cannot be null");
        }
        if (cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }
        // ✅ FIXED: BigDecimal is immutable, must reassign
        totalCost = totalCost.add(cost);
    }

    @Override
    public void recordPayment(BigDecimal paidAmount) {
        if (paidAmount == null) {
            throw new IllegalArgumentException("Paid amount cannot be null");
        }
        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Paid amount cannot be negative");
        }
        // ✅ FIXED: BigDecimal is immutable, must reassign
        totalPaid = totalPaid.add(paidAmount);
    }

    @Override
    public BigDecimal getTotalCost() {
        return totalCost;
    }

    @Override
    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    @Override
    public BigDecimal getUnpaidCost() {
        return totalCost.subtract(totalPaid);
    }
}