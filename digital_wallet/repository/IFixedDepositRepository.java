package digital_wallet.repository;

import digital_wallet.model.FixedDeposit;

public interface IFixedDepositRepository {
    void createFixedDeposit(FixedDeposit fixedDeposit);
    FixedDeposit getFixedDeposit(String userId);
    void updateFixedDeposit(FixedDeposit fixedDeposit);
    void removeFixedDeposit(String userId);
    boolean hasFixedDeposit(String userId);
}