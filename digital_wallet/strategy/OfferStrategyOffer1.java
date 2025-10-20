package digital_wallet.strategy;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import digital_wallet.model.Account;
import digital_wallet.model.Amount;
import digital_wallet.model.Currency;
import digital_wallet.model.offer.IOfferTypeData;
import digital_wallet.model.offer.OfferTypeDataOffer1;
import digital_wallet.model.transaction.Transaction;
import digital_wallet.model.transaction.TransactionType;
import digital_wallet.model.transaction.TransactionTypeDataOffer;
import digital_wallet.service.AccountService;
import digital_wallet.service.TransactionService;

import static digital_wallet.service.OfferService.SYSTEM_ACCOUNT_OFFER;

@AllArgsConstructor
public class OfferStrategyOffer1 implements IOfferStrategy {
    private AccountService accountService;
    private TransactionService transactionService;

    @Override
    public void apply(@NonNull IOfferTypeData offerTypeData) {
        final OfferTypeDataOffer1 offer1Data = (OfferTypeDataOffer1)offerTypeData;
        final Transaction transaction = offer1Data.getTransaction();
        Account srcAccount = accountService.getAccountById(transaction
                .getSrcAcc());
        Account dstAccount = accountService
                .getAccountById(transaction.getSrcAcc());

        if (srcAccount.getBalance().isEqualTo(dstAccount.getBalance())) {
            final Account systemAccountOffer
                    = accountService.getAccountById(SYSTEM_ACCOUNT_OFFER); //  enough balance here.

            transactionService.createTransaction(new Amount(10.0,

                            Currency.FKR),
                    systemAccountOffer,
                    srcAccount,
                    TransactionType.OFFER,
                    new TransactionTypeDataOffer("offer1"));
            transactionService.createTransaction(new Amount(10.0,
                            Currency.FKR),
                    systemAccountOffer,
                    dstAccount,
                    TransactionType.OFFER,
                    new TransactionTypeDataOffer("offer1"));
        }
    }
}
