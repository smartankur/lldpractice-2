package digital_wallet.repository;

import lombok.NonNull;
import digital_wallet.model.Account;
import digital_wallet.model.AccountType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountRepositoryInMemory implements IAccountRepository {

    // Map from userId to account.
    private Map<String, Account> accounts = new HashMap<>();

    @Override
    public void add(@NonNull final Account newAccount) {
        accounts.put(newAccount.getUserId(), newAccount);
    }

    @Override
    public Account getAccountByUserId(@NonNull final String userId) {
        return accounts.get(userId);
    }

    @Override
    public void update(Account account) {
        accounts.put(account.getUserId(), account);
    }

    @Override
    public Account getAccountById(String accountId) {
        return accounts.values().stream()
                .filter(acc -> acc.getId().equals(accountId))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Collection<Account> getAllAccounts(@NonNull final AccountType type) {
        return accounts.values().stream()
                .filter(acc ->
                        acc.getType().equals(type))
                .collect(Collectors.toList());
    }
}
