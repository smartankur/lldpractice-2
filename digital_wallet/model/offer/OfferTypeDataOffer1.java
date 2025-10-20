package digital_wallet.model.offer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import digital_wallet.model.transaction.Transaction;

@AllArgsConstructor
@Getter
public class OfferTypeDataOffer1 implements IOfferTypeData {

    private Transaction transaction;
}
