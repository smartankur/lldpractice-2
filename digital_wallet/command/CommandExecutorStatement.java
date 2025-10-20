package digital_wallet.command;

import digital_wallet.model.Account;
import digital_wallet.model.FixedDeposit;
import digital_wallet.model.transaction.Transaction;
import digital_wallet.service.AccountService;
import digital_wallet.service.TransactionService;
import digital_wallet.observer.FixedDepositService;
import lombok.AllArgsConstructor;
import java.util.List;

@AllArgsConstructor
public class CommandExecutorStatement implements ICommandExecutor {
    private AccountService accountService;
    private TransactionService transactionService;
    private FixedDepositService fixedDepositService;

    @Override
    public void execute(String[] commandParts) {
        final String userId = commandParts[1];
        final Account userAccount = accountService.getAccountByUserId(userId);
        final List<Transaction> statement = transactionService.getStatement(userId);

        for (Transaction transaction : statement) {
            if (transaction.getSrcAcc().equals(userAccount.getId())) {
                System.out.println(transaction.getDstAcc() + " debit " + transaction.getSrcAcc());
            } else {
                System.out.println(transaction.getSrcAcc() + " credit " + transaction.getSrcAcc());
            }
        }

        // Show FD info if exists
        FixedDeposit fd = fixedDepositService.getFixedDeposit(userId);
        if (fd != null && fd.isActive()) {
            System.out.println("FD Amount: " + fd.getFdAmount().getValue() +
                    " | Remaining Transactions: " + fd.getRemainingTransactions());
        }
    }
}
