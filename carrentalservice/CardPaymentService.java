package carrentalservice;

public class CardPaymentService implements IPaymentService {

    private CardPaymentDetails cardPaymentDetails;

    public CardPaymentService paymentDetails(IPaymentDetails paymentDetails) {
        this.cardPaymentDetails = (CardPaymentDetails) paymentDetails;
        return this;
    }

    @Override
    public boolean performPayment() {
        return true;
    }
}
