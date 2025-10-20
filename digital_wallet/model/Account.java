package digital_wallet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Account {
    private String id;
    private String userId;
    private Amount balance;
    private int transactionsCount = 0;
    private AccountType type;

    public boolean hasMinBalance(Amount amount) {
        return balance.isMoreThan(amount);
    }

    public void reduceBalance(Amount amount) {
        balance.reduceBy(amount);
    }

    public void increaseBalance(Amount amount) {
        balance.increaseBy(amount);
    }

    public void incrementTransaction(int byCount) {
        transactionsCount += byCount;
    }
}
