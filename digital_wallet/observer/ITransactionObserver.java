package digital_wallet.observer;

import lombok.NonNull;
import digital_wallet.model.transaction.Transaction;

public interface ITransactionObserver {
    void onTransactionCreate(@NonNull Transaction newTransaction);
}
