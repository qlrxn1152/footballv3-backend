package io.github.qlrxn1152.footballv3.teammatchgoal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamMatchGoalCreateRequest {

    @NotNull
    @Valid
    private List<TeamMatchGoalScorerRequest> homeScorers;

    @NotNull
    @Valid
    private List<TeamMatchGoalScorerRequest> awayScorers;
}
