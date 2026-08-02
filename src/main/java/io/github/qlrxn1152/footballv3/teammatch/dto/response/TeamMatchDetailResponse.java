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
public class TeamMatchDetailResponse {

    private Long matchId;
    private TeamMatchStatus status;

    private Long homeTeamId;
    private String homeTeamName;
    private int homeTeamRating;

    private Long awayTeamId;
    private String awayTeamName;
    private Integer awayTeamRating;

    private Integer homeScore;
    private Integer awayScore;



    private Long winnerTeamId;
    private String winnerTeamName;

    private LocalDateTime playedAt;

    public static TeamMatchDetailResponse of(TeamMatch teamMatch, TeamMatchResult teamMatchResult) {

        if (teamMatch.getStatus() == TeamMatchStatus.MATCHED) {
            return new TeamMatchDetailResponse(
                    teamMatch.getId(),
                    teamMatch.getStatus(),
                    teamMatch.getHomeTeam().getId(),
                    teamMatch.getHomeTeam().getTeamName(),
                    teamMatch.getHomeTeam().getRating(),

                    teamMatch.getAwayTeam().getId(),
                    teamMatch.getAwayTeam().getTeamName(),
                    teamMatch.getAwayTeam().getRating(),

                    null,
                    null,
                    null,
                    null,
                    teamMatch.getPlayedAt()
            );
        } else if (teamMatch.getStatus() == TeamMatchStatus.COMPLETED) {

            Team winnerTeam = teamMatchResult.getWinnerTeam();

            return new TeamMatchDetailResponse(
                    teamMatch.getId(),
                    teamMatch.getStatus(),
                    teamMatch.getHomeTeam().getId(),
                    teamMatch.getHomeTeam().getTeamName(),
                    teamMatch.getHomeTeam().getRating(),

                    teamMatch.getAwayTeam().getId(),
                    teamMatch.getAwayTeam().getTeamName(),
                    teamMatch.getAwayTeam().getRating(),
                    teamMatchResult.getHomeScore(),
                    teamMatchResult.getAwayScore(),
                    winnerTeam == null ? null : winnerTeam.getId(),
                    winnerTeam == null ? null : winnerTeam.getTeamName(),
                    teamMatch.getPlayedAt()
            );
        }

        // PENDING
        return new TeamMatchDetailResponse(
                teamMatch.getId(),
                teamMatch.getStatus(),
                teamMatch.getHomeTeam().getId(),
                teamMatch.getHomeTeam().getTeamName(),
                teamMatch.getHomeTeam().getRating(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                teamMatch.getPlayedAt()
        );


    }





}
