package orderdeliveryservice.model;

import lombok.Getter;

@Getter
public enum CalculationWindow {

    HOURLY(3600),
    MINUTE(60),
    SECONDS(1);
    private final int seconds;

    CalculationWindow(int seconds) {
        this.seconds = seconds;
    }
}
