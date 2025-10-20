package multithreadedpubsubmodel;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KafkaQueue {

    private List<Topic> topics;
}
