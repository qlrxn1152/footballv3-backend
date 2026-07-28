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
public class TeamMatchCreateResponse {

    private Long matchId;

    private Long homeTeamId;
    private String homeTeamName;

    private TeamMatchStatus status;

    private LocalDateTime playedAt;
    private LocalDateTime createdAt;

    public static TeamMatchCreateResponse of(TeamMatch teamMatch) {
        return new TeamMatchCreateResponse(
                teamMatch.getId(),
                teamMatch.getHomeTeam().getId(),
                teamMatch.getHomeTeam().getTeamName(),
                teamMatch.getStatus(),
                teamMatch.getPlayedAt(),
                teamMatch.getCreatedAt()
        );
    }
}
