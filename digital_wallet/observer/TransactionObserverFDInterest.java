package digital_wallet.observer;

import digital_wallet.model.transaction.Transaction;
import digital_wallet.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class TransactionObserverFDInterest implements ITransactionObserver {
    private AccountService accountService;
    private FixedDepositService fixedDepositService;
    
    @Override
    public void onTransactionCreate(@NonNull Transaction newTransaction) {
        // FD processing is handled by FixedDepositService itself
        // This observer can be used for additional FD-related logic if needed
    }
}