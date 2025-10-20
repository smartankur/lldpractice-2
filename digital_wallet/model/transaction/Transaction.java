package digital_wallet.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import digital_wallet.model.Amount;

import java.util.Date;

@AllArgsConstructor
@Getter
public class Transaction {
    private String id;
    private String srcAcc;
    private String srcUserId;
    private String dstAcc;
    private String dstUserId;
    private Amount amount;
    private TransactionType type;
    private ITransactionTypeData transactionTypeData;
    private Date timestamp;
}

/**
 *
 * Double account
 *
 * Transaction happens
 * - one account is debited and another account is credited.
 *
 */
