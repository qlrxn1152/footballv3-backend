package io.github.qlrxn1152.footballv3.teammatch.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchGoal;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private List<TeamMatchGoalScorerResponse> homeScorers;
    private List<TeamMatchGoalScorerResponse> awayScorers;

    private Long winnerTeamId;
    private String winnerTeamName;

    private LocalDateTime playedAt;

    public static TeamMatchDetailResponse of(TeamMatch teamMatch, TeamMatchResult teamMatchResult, List<TeamMatchGoal> teamMatchGoals) {


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
                    List.of(),
                    List.of(),
                    null,
                    null,
                    teamMatch.getPlayedAt()
            );
        } else if (teamMatch.getStatus() == TeamMatchStatus.COMPLETED) {

            Team winnerTeam = teamMatchResult.getWinnerTeam();
            List<TeamMatchGoalScorerResponse> homeScorers = toScorers(teamMatchGoals, teamMatch.getHomeTeam().getId());
            List<TeamMatchGoalScorerResponse> awayScorers = toScorers(teamMatchGoals, teamMatch.getAwayTeam().getId());


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
                    homeScorers,
                    awayScorers,
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
                List.of(),
                List.of(),
                null,
                null,
                teamMatch.getPlayedAt()
        );
    }


    private static List<TeamMatchGoalScorerResponse> toScorers(List<TeamMatchGoal> teamMatchGoals, Long teamId) {
        return teamMatchGoals.stream()
                .filter(goal -> goal.getTeam().getId().equals(teamId))
                .map(TeamMatchGoalScorerResponse::of)
                .toList();
    }





}
