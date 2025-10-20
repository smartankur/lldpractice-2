package digital_wallet.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import digital_wallet.model.*;
import digital_wallet.model.offer.IOfferTypeData;
import digital_wallet.strategy.IOfferStrategy;

import java.util.Map;

@AllArgsConstructor
public class OfferService {
    public static final String SYSTEM_ACCOUNT_OFFER = "system-account-offer";

    private Map<String, IOfferStrategy> offerStrategies;

    public void applyOffer(@NonNull final String offerName,
                           @NonNull final IOfferTypeData offerTypeData) {
        offerStrategies.get(offerName).apply(offerTypeData);
    }
}
