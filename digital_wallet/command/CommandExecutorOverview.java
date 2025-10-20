package digital_wallet.command;

import digital_wallet.model.Account;
import digital_wallet.model.AccountType;
import digital_wallet.model.FixedDeposit;
import digital_wallet.service.AccountService;
import digital_wallet.observer.FixedDepositService;
import lombok.AllArgsConstructor;
import java.util.Collection;

@AllArgsConstructor
public class CommandExecutorOverview implements ICommandExecutor {
    private AccountService accountService;
    private FixedDepositService fixedDepositService;

    @Override
    public void execute(String[] commandParts) {
        Collection<Account> accounts = accountService.getAllAccounts(AccountType.USER);

        for (Account account : accounts) {
            System.out.print(account.getUserId() + " " + account.getBalance().getValue());

            // Show FD info if exists
            FixedDeposit fd = fixedDepositService.getFixedDeposit(account.getUserId());
            if (fd != null && fd.isActive()) {
                System.out.print(" | FD: " + fd.getFdAmount().getValue() +
                        " | Remaining: " + fd.getRemainingTransactions());
            }
            System.out.println();
        }
    }
}

