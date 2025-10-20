package digital_wallet.observer;

import digital_wallet.model.*;
import digital_wallet.model.transaction.*;
import digital_wallet.repository.IFixedDepositRepository;
import digital_wallet.repository.IAccountRepository;
import digital_wallet.repository.ITransactionRepository;
import digital_wallet.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static digital_wallet.service.OfferService.SYSTEM_ACCOUNT_OFFER;

@AllArgsConstructor
public class FixedDepositService implements ITransactionObserver {
    private IFixedDepositRepository fixedDepositRepository;
    private IAccountRepository accountRepository;
    private ITransactionRepository transactionRepository;
    private TransactionService transactionService;
    
    public void createFixedDeposit(String userId, Amount fdAmount) {
        Account account = accountRepository.getAccountByUserId(userId);
        
        if (account == null) {
            throw new RuntimeException("Account not found for user: " + userId);
        }
        
        if (!account.hasMinBalance(fdAmount)) {
            throw new RuntimeException("Insufficient balance for fixed deposit");
        }
        
        // Remove any existing FD
        if (fixedDepositRepository.hasFixedDeposit(userId)) {
            fixedDepositRepository.removeFixedDeposit(userId);
        }
        
        // Create new FD
        FixedDeposit fixedDeposit = new FixedDeposit(userId, fdAmount);
        fixedDepositRepository.createFixedDeposit(fixedDeposit);
    }
    
    @Override
    public void onTransactionCreate(@NonNull final Transaction newTransaction) {
        // Check FD for source account (debit transaction)
        if (newTransaction.getType() != TransactionType.TRANSFER) {
            return;
        }
        
        String srcUserId = newTransaction.getSrcAcc();
        String dstUserId = newTransaction.getDstAcc();
        
        // Process FD for source account
        processFixedDepositForUser(srcUserId);
        
        // Process FD for destination account
        processFixedDepositForUser(dstUserId);
    }
    
    private void processFixedDepositForUser(String userId) {
        if (!fixedDepositRepository.hasFixedDeposit(userId)) {
            return;
        }
        
        FixedDeposit fd = fixedDepositRepository.getFixedDeposit(userId);
        if (!fd.isActive()) {
            return;
        }
        
        Account account = accountRepository.getAccountByUserId(userId);
        
        // Check if balance is maintained
        if (!account.hasMinBalance(fd.getFdAmount())) {
            // FD broken - remove it
            fd.setActive(false);
            fixedDepositRepository.removeFixedDeposit(userId);
            return;
        }
        
        // Decrement transaction count
        fd.decrementTransaction();
        
        // Check if FD is matured
        if (fd.isMatured()) {
            // Give interest
            Amount interest = new Amount(10.0, Currency.FKR);
            
            // Create interest transaction
            ITransactionTypeData data = new TransactionTypeDataOffer(SYSTEM_ACCOUNT_OFFER);
            
            transactionService.createTransaction(
                interest,
                null,
                account,
                TransactionType.OFFER,
                data
            );
            
            // Remove FD after maturity
            fixedDepositRepository.removeFixedDeposit(userId);
        } else {
            // Update FD
            fixedDepositRepository.updateFixedDeposit(fd);
        }
    }
    
    public FixedDeposit getFixedDeposit(String userId) {
        return fixedDepositRepository.getFixedDeposit(userId);
    }
}