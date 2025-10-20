package digital_wallet.service;

import digital_wallet.exception.InsufficientBalanceException;
import digital_wallet.model.Account;
import digital_wallet.model.Amount;
import digital_wallet.model.transaction.ITransactionTypeData;
import digital_wallet.model.transaction.Transaction;
import digital_wallet.model.transaction.TransactionType;
import digital_wallet.observer.TransactionObserverManager;
import digital_wallet.repository.IAccountRepository;
import digital_wallet.repository.ITransactionRepository;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class TransactionService {
    private IAccountRepository accountRepository;

    private ITransactionRepository transactionRepository;
    private TransactionObserverManager transactionObserverManager;

    public void createTransaction(Amount amount,
                                  Account srcAccount,
                                  Account dstAccount,
                                  TransactionType type,
                                  ITransactionTypeData data) {
        if (!srcAccount.hasMinBalance(amount)) {
            throw new InsufficientBalanceException();
        }

        final Transaction transaction = new Transaction(UUID.randomUUID().toString(),
                srcAccount.getId(),
                srcAccount.getUserId(),
                dstAccount.getId(),
                dstAccount.getUserId(),
                amount,
                type,
                data,
                new Date());
        transactionRepository.add(transaction);
        srcAccount.reduceBalance(amount);
        srcAccount.incrementTransaction(1);
        accountRepository.update(srcAccount);
        dstAccount.increaseBalance(amount);
        dstAccount.incrementTransaction(1);
        accountRepository.update(dstAccount);

        transactionObserverManager.onTransactionCreate(transaction);
    }

    public List<Transaction> getStatement(String userId) {
        return transactionRepository.getTransactionsForUser(userId);
    }
}
