package digital_wallet.service;

import digital_wallet.model.Account;
import digital_wallet.model.AccountType;
import digital_wallet.model.Amount;
import digital_wallet.model.transaction.TransactionType;
import digital_wallet.repository.IAccountRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
public class AccountService {
    private IAccountRepository accountRepository;
    private TransactionService transactionService;

    public Account createAccount(@NonNull final String userId,
                                 @NonNull final Amount initialBalance) {
        final Account newAccount
                = new Account(UUID.randomUUID().toString(), userId, initialBalance, 0, AccountType.USER);

        accountRepository.add(newAccount);
        return newAccount;
    }


    public void transferMoney(String srcUserId, String dstUserId, Amount amount) {
        final Account srcAccount = accountRepository
                .getAccountByUserId(srcUserId);
        final Account dstAccount = accountRepository
                .getAccountByUserId(dstUserId);
        transactionService
                .createTransaction(amount, srcAccount,
                        dstAccount, TransactionType.TRANSFER, null);
    }

    public Account getAccountById(String accountId) {
        return accountRepository.getAccountById(accountId);
    }

    public Collection<Account> getAllAccounts(@NonNull final AccountType type) {
        return accountRepository.getAllAccounts(type);
    }

    public Account getAccountByUserId(String userId) {
        return accountRepository.getAccountByUserId(userId);
    }
}
