package digital_wallet.command;

import digital_wallet.model.Amount;
import digital_wallet.model.Currency;
import digital_wallet.service.AccountService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommandExecutorCreateWallet implements ICommandExecutor {
    private AccountService accountService;

    @Override
    public void execute(String[] commandParts) {
        final String userId = commandParts[1];
        final Double balance = Double.parseDouble(commandParts[2]);
        accountService.createAccount(userId,
                new Amount(balance, Currency.FKR));
    }
}
