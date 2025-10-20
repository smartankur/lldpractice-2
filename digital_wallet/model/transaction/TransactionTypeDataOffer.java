package digital_wallet.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransactionTypeDataOffer implements ITransactionTypeData {
    private String offerId;
}
