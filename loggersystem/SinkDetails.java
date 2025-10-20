package loggersystem;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SinkDetails {

    private Message message;

    public abstract void validate();
}
