package digital_wallet.command;

import lombok.AllArgsConstructor;
import digital_wallet.model.Amount;
import digital_wallet.model.Currency;
import digital_wallet.service.AccountService;

@AllArgsConstructor
public class CommandExecutorTransferMoney implements ICommandExecutor {

    private AccountService accountService;

    @Override
    public void execute(String[] commandParts) {
        final String srcUserId = commandParts[1];
        final String dstUserId = commandParts[2];
        final Amount amount = new Amount(Double
                .parseDouble(commandParts[3]), Currency.FKR);
        accountService.transferMoney(srcUserId, dstUserId, amount);
    }
}
