package io.github.qlrxn1152.footballv3.teammatch.dto.response;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchGoal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMatchGoalScorerResponse {

    private Long memberId;
    private String username;
    private int goalCount;

    public static TeamMatchGoalScorerResponse of(TeamMatchGoal teamMatchGoal) {
        return new TeamMatchGoalScorerResponse(
                teamMatchGoal.getMember().getId(),
                teamMatchGoal.getMember().getUsername(),
                teamMatchGoal.getGoalCount()
        );
    }
}
