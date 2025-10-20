package digital_wallet.command;

import lombok.AllArgsConstructor;
import digital_wallet.model.offer.OfferTypeDataOffer2;
import digital_wallet.service.OfferService;

@AllArgsConstructor
public class CommandExecutorOffer2 implements ICommandExecutor {
    private OfferService offerService;

    @Override
    public void execute(String[] commandParts) {
        offerService
                .applyOffer("offer2",
                        new OfferTypeDataOffer2());
    }
}
