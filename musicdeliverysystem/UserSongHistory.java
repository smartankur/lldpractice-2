package musicdeliverysystem;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserSongHistory {
    private final Integer songId;
    private final long lastPlayedTime;
}
