package digital_wallet.strategy;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import digital_wallet.model.Account;
import digital_wallet.model.AccountType;
import digital_wallet.model.Amount;
import digital_wallet.model.offer.IOfferTypeData;
import digital_wallet.model.transaction.TransactionType;
import digital_wallet.model.transaction.TransactionTypeDataOffer;
import digital_wallet.service.AccountService;
import digital_wallet.service.TransactionService;

import java.util.*;

import static digital_wallet.service.OfferService.SYSTEM_ACCOUNT_OFFER;

@AllArgsConstructor
public class OfferStrategyOffer2 implements IOfferStrategy {
    private AccountService accountService;
    private TransactionService transactionService;
    private Map<Integer, Amount> offer2Amounts;

    @Override
    public void apply(@NonNull IOfferTypeData offerTypeData) {
        final List<Account> allAccounts = new ArrayList<>(accountService.getAllAccounts(AccountType.USER));
        final Account offerSystemAccount = accountService.getAccountById(SYSTEM_ACCOUNT_OFFER);

        // TODO: Fix sorting as per the problem statement.
        Collections.sort(allAccounts, new Comparator<Account>() {
            public int compare(Account a1, Account a2) {
                return a2.getTransactionsCount() - a1.getTransactionsCount();
            }
        });

        for (int i = 0; i < Math.min(allAccounts.size(), 3); i++) {
            transactionService.createTransaction(offer2Amounts.get(i),
                    offerSystemAccount,
                    allAccounts.get(i),
                    TransactionType.OFFER,
                    new TransactionTypeDataOffer("offer2"));
        }
    }
}
