package digital_wallet.model.offer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Offer {
    private String id;
    private OfferType type;
}
