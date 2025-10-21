package orderdeliveryservice.repository;

import orderdeliveryservice.model.Driver;

/**
 * Repository interface for Driver persistence operations.
 */
public interface IDriverRepository {
    
    /**
     * Saves a driver to the repository.
     * 
     * @param driver the driver to save
     * @throws IllegalArgumentException if driver already exists
     */
    void save(Driver driver);
    
    /**
     * Finds a driver by ID.
     * 
     * @param driverId the driver ID to search for
     * @return the driver, or null if not found
     */
    Driver findById(String driverId);
    
    /**
     * Checks if a driver exists.
     * 
     * @param driverId the driver ID to check
     * @return true if driver exists, false otherwise
     */
    boolean exists(String driverId);
}