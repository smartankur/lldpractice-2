package digital_wallet.observer;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import digital_wallet.model.offer.OfferTypeDataOffer1;
import digital_wallet.model.transaction.Transaction;
import digital_wallet.model.transaction.TransactionType;
import digital_wallet.service.AccountService;
import digital_wallet.service.OfferService;

@AllArgsConstructor
public class TransactionObserverOffer1 implements ITransactionObserver {
    private AccountService accountService;
    private OfferService offerService;

    @Override
    public void onTransactionCreate(@NonNull Transaction newTransaction) {
        if (newTransaction.getType() != TransactionType.TRANSFER) {
            return;
        }
        offerService.applyOffer("offer1",
                new OfferTypeDataOffer1(newTransaction));
    }
}
