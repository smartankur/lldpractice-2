package musicdeliverysystem;


import lombok.*;

@AllArgsConstructor
@Getter
@Builder
public class Song {
    private final Integer songId;
    private final String songTitle;
}
