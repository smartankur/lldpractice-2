package orderdeliveryservice.repository;

import orderdeliveryservice.model.Driver;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of Driver repository.
 * In production, this would interact with a database.
 */
public class DriverRepository implements IDriverRepository {
    
    private final Map<String, Driver> drivers;
    
    public DriverRepository() {
        this.drivers = new HashMap<>();
    }
    
    @Override
    public void save(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }
        if (exists(driver.getDriverId())) {
            throw new IllegalArgumentException("Driver already exists: " + driver.getDriverId());
        }
        drivers.put(driver.getDriverId(), driver);
    }
    
    @Override
    public Driver findById(String driverId) {
        return drivers.get(driverId);
    }
    
    @Override
    public boolean exists(String driverId) {
        return drivers.containsKey(driverId);
    }
}