package io.github.qlrxn1152.footballv3.teammatchgoal.dto.response;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalScorerRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMatchGoalCreateResponse {

    private Long matchId;

    private int homeScore;
    private int awayScore;

    private List<TeamMatchGoalScorerRequest> homeScorers;
    private List<TeamMatchGoalScorerRequest> awayScorers;

    public static TeamMatchGoalCreateResponse of(TeamMatchResult teamMatchResult, List<TeamMatchGoalScorerRequest> homeScorers,  List<TeamMatchGoalScorerRequest> awayScorers) {
        return new TeamMatchGoalCreateResponse(
                teamMatchResult.getTeamMatch().getId(),
                teamMatchResult.getHomeScore(),
                teamMatchResult.getAwayScore(),
                homeScorers,
                awayScorers

        );
    }

}
