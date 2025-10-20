package carrentalservice;

public interface  IPaymentService {

    public IPaymentService paymentDetails(IPaymentDetails iPaymentDetails);

    public boolean performPayment();
}
