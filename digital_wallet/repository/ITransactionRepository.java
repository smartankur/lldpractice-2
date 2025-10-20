package digital_wallet.repository;

import digital_wallet.model.transaction.Transaction;

import java.util.List;

public interface ITransactionRepository {
    void add(Transaction transaction);

    List<Transaction> getTransactionsForUser(String userId);
}
