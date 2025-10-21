package orderdeliveryservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class Rate {
   private BigDecimal rate;
   private CalculationWindow window;
}
