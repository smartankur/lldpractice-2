package orderdeliveryservice.repository;

import orderdeliveryservice.model.Delivery;

import java.util.List;

/**
 * Repository interface for Delivery persistence operations.
 */
public interface IDeliveryRepository {
    
    /**
     * Saves a delivery to the repository.
     * 
     * @param delivery the delivery to save
     */
    void save(Delivery delivery);
    
    /**
     * Retrieves all deliveries.
     * 
     * @return list of all deliveries
     */
    List<Delivery> findAll();
    
    /**
     * Retrieves unpaid deliveries up to a given timestamp.
     * Uses efficient two-pointer approach.
     * 
     * @param timestamp the upper bound timestamp
     * @return list of unpaid deliveries ending at or before timestamp
     */
    List<Delivery> findUnpaidDeliveriesUpTo(long timestamp);
    
    /**
     * Marks deliveries as paid up to a given timestamp.
     * 
     * @param timestamp the upper bound timestamp
     * @return number of deliveries marked as paid
     */
    int markAsPaidUpTo(long timestamp);
    
    /**
     * Gets the count of all deliveries.
     * 
     * @return total delivery count
     */
    int getTotalCount();
    
    /**
     * Gets the count of paid deliveries.
     * 
     * @return paid delivery count
     */
    int getPaidCount();
    
    /**
     * Gets the count of unpaid deliveries.
     * 
     * @return unpaid delivery count
     */
    int getUnpaidCount();
}