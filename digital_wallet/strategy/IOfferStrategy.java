package digital_wallet.strategy;

import lombok.NonNull;
import digital_wallet.model.offer.IOfferTypeData;

public interface IOfferStrategy {
    void apply(@NonNull IOfferTypeData offerTypeData);
}
