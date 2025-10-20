package digital_wallet.command;

import digital_wallet.model.Amount;
import digital_wallet.model.Currency;
import digital_wallet.observer.FixedDepositService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommandExecutorFixedDeposit implements ICommandExecutor {
    private FixedDepositService fixedDepositService;
    
    @Override
    public void execute(String[] commandParts) {
        if (commandParts.length != 3) {
            throw new IllegalArgumentException("Invalid FixedDeposit command format");
        }
        
        String userId = commandParts[1];
        double fdAmount = Double.parseDouble(commandParts[2]);
        
        fixedDepositService.createFixedDeposit(
            userId,
            new Amount(fdAmount, Currency.FKR)
        );
    }
}