package movieticketbookingsystem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Movie {
   private Long duration;
   private String id;
}
