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
public class TeamMatchCompletedResponse {

    private Long matchId;

    private Long homTeamId;
    private String homeTeamName;
    private int homeTeamRating;
    private int homeScore;

    private Long awayTeamId;
    private String awayTeamName;
    private int awayTeamRating;
    private int awayScore;

    private Long winnerTeamId;
    private String winnerTeamName;

    private TeamMatchStatus status;

    private LocalDateTime playedAt;
    private LocalDateTime completedAt;

    public static TeamMatchCompletedResponse of(TeamMatchResult teamMatchResult) {
        TeamMatch teamMatch = teamMatchResult.getTeamMatch();
        Team winnerTeam = teamMatchResult.getWinnerTeam();


        return new TeamMatchCompletedResponse(
                teamMatch.getId(),
                teamMatch.getHomeTeam().getId(),
                teamMatch.getHomeTeam().getTeamName(),
                teamMatch.getHomeTeam().getRating(),
                teamMatchResult.getHomeScore(),
                teamMatch.getAwayTeam().getId(),
                teamMatch.getAwayTeam().getTeamName(),
                teamMatch.getAwayTeam().getRating(),
                teamMatchResult.getAwayScore(),

                winnerTeam == null ? null : winnerTeam.getId(),
                winnerTeam == null ? null : winnerTeam.getTeamName(),

                teamMatch.getStatus(),
                teamMatch.getPlayedAt(),
                teamMatch.getCompletedAt()
                );
    }


}
