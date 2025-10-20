package digital_wallet.observer;

import lombok.NonNull;
import digital_wallet.model.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionObserverManager {
    private List<ITransactionObserver> observees = new ArrayList<>();

    public void onTransactionCreate(@NonNull final Transaction newTransaction) {
        for (ITransactionObserver observee: observees) {
            observee.onTransactionCreate(newTransaction);
        }
    }

    public void register(ITransactionObserver observee) {
        observees.add(observee);
    }
}
