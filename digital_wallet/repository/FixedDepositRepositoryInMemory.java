package digital_wallet.repository;

import digital_wallet.model.FixedDeposit;
import java.util.HashMap;
import java.util.Map;

public class FixedDepositRepositoryInMemory implements IFixedDepositRepository {
    private Map<String, FixedDeposit> fixedDeposits = new HashMap<>();
    
    @Override
    public void createFixedDeposit(FixedDeposit fixedDeposit) {
        fixedDeposits.put(fixedDeposit.getUserId(), fixedDeposit);
    }
    
    @Override
    public FixedDeposit getFixedDeposit(String userId) {
        return fixedDeposits.get(userId);
    }
    
    @Override
    public void updateFixedDeposit(FixedDeposit fixedDeposit) {
        fixedDeposits.put(fixedDeposit.getUserId(), fixedDeposit);
    }
    
    @Override
    public void removeFixedDeposit(String userId) {
        fixedDeposits.remove(userId);
    }
    
    @Override
    public boolean hasFixedDeposit(String userId) {
        return fixedDeposits.containsKey(userId);
    }
}