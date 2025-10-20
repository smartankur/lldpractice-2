package multithreadedpubsubmodel;


import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Builder
@Data
public class Topic {
    private String id;

    @Builder.Default
    private Map<Integer, Message> messages = Collections.synchronizedMap(new LinkedHashMap<>());
}
