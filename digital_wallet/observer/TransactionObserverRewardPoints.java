package digital_wallet.observer;

import lombok.NonNull;
import digital_wallet.model.Account;
import digital_wallet.model.transaction.Transaction;
import digital_wallet.model.transaction.TransactionType;
import digital_wallet.service.AccountService;
import digital_wallet.service.TransactionService;

public class TransactionObserverRewardPoints implements ITransactionObserver {
    private AccountService accountService;
    private TransactionService transactionService;

    @Override
    public void onTransactionCreate(@NonNull Transaction newTransaction) {
        if (newTransaction.getType() != TransactionType.TRANSFER) {
            return;
        }

        final Account srcAccount
                = accountService.getAccountById(newTransaction.getSrcAcc());
        final Account dstAccount
                = accountService.getAccountById(newTransaction.getDstAcc());
//        srcAccount.addRewardPoints(100);
    }
}
