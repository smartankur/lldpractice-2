package carrentalservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class CardPaymentDetails implements IPaymentDetails {
    private String name;
    private float amount;
    private String cardNumber;
    private String company;
}
