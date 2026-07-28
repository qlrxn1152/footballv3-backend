package io.github.qlrxn1152.footballv3.teammatch.dto.response;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMatchPendingResponse {

    private Long matchId;

    private Long homeTeamId;
    private String homeTeamName;
    private int homeTeamRating;

    private TeamMatchStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime playedAt;

    public static TeamMatchPendingResponse of(TeamMatch teamMatch) {
        return new TeamMatchPendingResponse(
                teamMatch.getId(),
                teamMatch.getHomeTeam().getId(),
                teamMatch.getHomeTeam().getTeamName(),
                teamMatch.getHomeTeam().getRating(),
                teamMatch.getStatus(),
                teamMatch.getCreatedAt(),
                teamMatch.getPlayedAt()
        );
    }
}
