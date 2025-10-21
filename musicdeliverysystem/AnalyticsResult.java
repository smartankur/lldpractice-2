package musicdeliverysystem;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class AnalyticsResult {
    private final Integer songId;
    private final Integer uniqueUserCount;
    private final Integer totalPlayCount;
    private final long createdAt;
}
