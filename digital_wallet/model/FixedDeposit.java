package digital_wallet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FixedDeposit {
    private String userId;
    private Amount fdAmount;
    private int remainingTransactions;
    private boolean isActive;
    
    public FixedDeposit(String userId, Amount fdAmount) {
        this.userId = userId;
        this.fdAmount = fdAmount;
        this.remainingTransactions = 5;
        this.isActive = true;
    }
    
    public void decrementTransaction() {
        if (remainingTransactions > 0) {
            remainingTransactions--;
        }
    }
    
    public boolean isMatured() {
        return remainingTransactions == 0 && isActive;
    }
}