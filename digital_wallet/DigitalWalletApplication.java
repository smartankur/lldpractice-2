package digital_wallet;

import digital_wallet.command.*;
import digital_wallet.observer.*;
import digital_wallet.repository.*;
import digital_wallet.service.*;
import digital_wallet.strategy.*;
import java.util.*;

import static digital_wallet.service.OfferService.SYSTEM_ACCOUNT_OFFER;

public class DigitalWalletApplication {

    public static void main(String[] args) {
        // Initialize repositories
        IAccountRepository accountRepository = new AccountRepositoryInMemory();
        ITransactionRepository transactionRepository = new TransactionRepositoryInMemory();
        IFixedDepositRepository fixedDepositRepository = new FixedDepositRepositoryInMemory();
        
        // Initialize transaction observer manager
        TransactionObserverManager transactionObserverManager = new TransactionObserverManager();
        TransactionService transactionService = new TransactionService(accountRepository, transactionRepository, transactionObserverManager);
        
        // Initialize services
        AccountService accountService = new AccountService(accountRepository, transactionService);
        
        FixedDepositService fixedDepositService = new FixedDepositService(
            fixedDepositRepository,
            accountRepository,
            transactionRepository,
            transactionService
        );
        
        // Initialize offer services
        IOfferStrategy offerStrategy1 = new OfferStrategyOffer1(accountService, transactionService);
        IOfferStrategy offerStrategy2 = new OfferStrategyOffer2(accountService, transactionService, Map.of());
        OfferService offerService = new OfferService(
            Map.of(SYSTEM_ACCOUNT_OFFER, offerStrategy1, "offer2", offerStrategy2)
        );
        
        // Register observers
        transactionObserverManager.register(new TransactionObserverOffer1(
            accountService,
            offerService
        ));
        transactionObserverManager.register(new TransactionObserverFDInterest(
            accountService,
            fixedDepositService
        ));
        transactionObserverManager.register(fixedDepositService);
        
        // Initialize command executors
        Map<String, ICommandExecutor> commandExecutors = new HashMap<>();
        commandExecutors.put("CreateWallet", new CommandExecutorCreateWallet(accountService));
        commandExecutors.put("TransferMoney", new CommandExecutorTransferMoney(accountService));
        commandExecutors.put("Statement", new CommandExecutorStatement(
            accountService,
            transactionService,
            fixedDepositService
        ));
        commandExecutors.put("Overview", new CommandExecutorOverview(
            accountService,
            fixedDepositService
        ));
        commandExecutors.put("Offer2", new CommandExecutorOffer2(offerService));
        commandExecutors.put("FixedDeposit", new CommandExecutorFixedDeposit(
            fixedDepositService
        ));
        
        CommandManager commandManager = new CommandManager(commandExecutors);
        
        // Sample execution
        String[] commands = {
            "CreateWallet Harry 100",
            "CreateWallet Ron 95.7",
            "CreateWallet Hermione 104",
            "CreateWallet Albus 200",
            "CreateWallet Draco 500",
            "Overview",
            "FixedDeposit Harry 50",
            "TransferMoney Albus Draco 30",
            "TransferMoney Hermione Harry 2",
            "TransferMoney Albus Ron 5",
            "Overview",
            "Statement Harry",
            "Statement Albus",
            "Offer2",
            "Overview"
        };
        
        for (String command : commands) {
            System.out.println("Executing: " + command);
            try {
                commandManager.executeCommand(command);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }
}