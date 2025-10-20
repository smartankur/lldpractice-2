package digital_wallet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Amount {
    private Double value;
    private Currency currency;

    public boolean isMoreThan(Amount amount) {
        if (amount.currency != currency) {
            throw new RuntimeException("Incomparable balances");
        }
        return value >= amount.value;
    }

    public void increaseBy(Amount amount) {
        if (amount.currency != currency) {
            throw new RuntimeException("Incomparable balances");
        }
        value += amount.value;
    }

    public void reduceBy(Amount amount) {
        if (amount.currency != currency) {
            throw new RuntimeException("Incomparable balances");
        }
        value -= amount.value;
    }

    public boolean isEqualTo(Amount amount) {
        if (amount.currency != currency) {
            throw new RuntimeException("Incomparable balances");
        }
        return value == amount.value;
    }
}
