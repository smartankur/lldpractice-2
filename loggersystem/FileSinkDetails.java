package loggersystem;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileSinkDetails extends SinkDetails {
    private String location;
    private String fileName;

    @Override
    public synchronized void validate() {
        if(location == null){
            throw new IllegalArgumentException("Location can't be null");
        }

        if(fileName == null) {
            throw new IllegalArgumentException("File name can't be null");
        }
    }
}
