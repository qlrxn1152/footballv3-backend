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
public class TeamMatchAcceptResponse {

    private Long matchId;

    private Long homeTeamId;
    private String homeTeamName;
    private int homeTeamRating;

    private Long awayTeamId;
    private String awayTeamName;
    private int awayTeamRating;

    private TeamMatchStatus status;

    private LocalDateTime playedAt;
    private LocalDateTime matchedAt; // MATCHED 로 언제 변경된건지  ?

    public static TeamMatchAcceptResponse of(TeamMatch teamMatch) {
        return new TeamMatchAcceptResponse(
                teamMatch.getId(),
                teamMatch.getHomeTeam().getId(),
                teamMatch.getHomeTeam().getTeamName(),
                teamMatch.getHomeTeam().getRating(),

                teamMatch.getAwayTeam().getId(),
                teamMatch.getAwayTeam().getTeamName(),
                teamMatch.getAwayTeam().getRating(),

                teamMatch.getStatus(),
                teamMatch.getPlayedAt(),
                teamMatch.getMatchedAt()
        );
    }

}
