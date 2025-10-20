package digital_wallet.repository;

import lombok.NonNull;
import digital_wallet.model.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRepositoryInMemory implements ITransactionRepository {

    private List<Transaction> transactions = new ArrayList<>();

    @Override
    public void add(@NonNull final Transaction transaction) {
        transactions.add(transaction);
    }

    @Override
    public List<Transaction> getTransactionsForUser(String userId) {
        return transactions.stream()
                .filter(transaction ->
                        transaction.getSrcUserId().equals(userId)
                                || transaction.getDstUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
