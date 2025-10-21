package orderdeliveryservice.repository;

import orderdeliveryservice.model.Delivery;
import orderdeliveryservice.model.DeliveryStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of Delivery repository.
 * Uses ArrayList with two-pointer technique for efficient payment processing.
 *
 * Design decision: ArrayList over TreeSet because:
 * - Deliveries arrive in chronological order
 * - No duplicate endTime data loss
 * - Better performance for sequential access
 * - Two-pointer gives O(1) amortized payment processing
 */
public class DeliveryRepository implements IDeliveryRepository {

    private final List<Delivery> deliveries;
    private int lastPaidIndex; // Two-pointer for efficient payment tracking

    public DeliveryRepository() {
        this.deliveries = new ArrayList<>();
        this.lastPaidIndex = 0;
    }

    @Override
    public void save(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery cannot be null");
        }
        deliveries.add(delivery);
    }

    @Override
    public List<Delivery> findAll() {
        return new ArrayList<>(deliveries);
    }

    @Override
    public List<Delivery> findUnpaidDeliveriesUpTo(long timestamp) {
        List<Delivery> unpaidDeliveries = new ArrayList<>();

        // Start from last paid index (two-pointer technique)
        for (int i = lastPaidIndex; i < deliveries.size(); i++) {
            Delivery delivery = deliveries.get(i);
            if (delivery.getEndTime() <= timestamp) {
                unpaidDeliveries.add(delivery);
            } else {
                // Since deliveries are ordered by completion time, we can stop
                break;
            }
        }

        return unpaidDeliveries;
    }

    @Override
    public int markAsPaidUpTo(long timestamp) {
        int count = 0;

        // Two-pointer approach: scan from last paid position
        while (lastPaidIndex < deliveries.size()) {
            Delivery delivery = deliveries.get(lastPaidIndex);

            if (delivery.getEndTime() <= timestamp) {
                delivery.setStatus(DeliveryStatus.PAID);
                lastPaidIndex++;
                count++;
            } else {
                // Deliveries are ordered, so we can stop here
                break;
            }
        }

        return count;
    }

    @Override
    public int getTotalCount() {
        return deliveries.size();
    }

    @Override
    public int getPaidCount() {
        return lastPaidIndex;
    }

    @Override
    public int getUnpaidCount() {
        return deliveries.size() - lastPaidIndex;
    }
}