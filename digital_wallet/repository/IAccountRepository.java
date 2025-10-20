package digital_wallet.repository;

import lombok.NonNull;
import digital_wallet.model.Account;
import digital_wallet.model.AccountType;

import java.util.Collection;

public interface IAccountRepository {
    void add(Account newAccount);

    Account getAccountByUserId(String userId);

    void update(Account account);

    Account getAccountById(String accountId);

    Collection<Account> getAllAccounts(@NonNull final AccountType type);
}
