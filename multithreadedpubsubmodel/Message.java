package multithreadedpubsubmodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Message {
    private String id;
}