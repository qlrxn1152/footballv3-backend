package io.github.qlrxn1152.footballv3.teammatch.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMatchResultResponse {

    private Long matchId;

    private Long homeTeamId;
    private String homeTeamName;
    private int homeScore;

    private Long awayTeamId;
    private String awayTeamName;
    private int awayScore;

    private Long winnerTeamId;
    private String winnerTeamName;

    private TeamMatchStatus status;
    private LocalDateTime completedAt; // 언제 경기가 종료된거임 ?

    public static TeamMatchResultResponse of(TeamMatchResult teamMatchResult) {
        TeamMatch teamMatch = teamMatchResult.getTeamMatch();
        Team winnerTeam = teamMatchResult.getWinnerTeam();

        return new TeamMatchResultResponse(
                teamMatch.getId(),
                teamMatch.getHomeTeam().getId(),
                teamMatch.getHomeTeam().getTeamName(),
                teamMatchResult.getHomeScore(),

                teamMatch.getAwayTeam().getId(),
                teamMatch.getAwayTeam().getTeamName(),
                teamMatchResult.getAwayScore(),

                winnerTeam == null ? null : winnerTeam.getId(),
                winnerTeam == null ? null : winnerTeam.getTeamName(),

                teamMatch.getStatus(),
                teamMatchResult.getCompletedAt()
        );
    }


}
