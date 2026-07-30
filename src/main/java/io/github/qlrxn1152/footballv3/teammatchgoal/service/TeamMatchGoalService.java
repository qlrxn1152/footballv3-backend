package io.github.qlrxn1152.footballv3.teammatchgoal.service;

import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalCreateRequest;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.response.TeamMatchGoalCreateResponse;

public interface TeamMatchGoalService {

    TeamMatchGoalCreateResponse registerGoals(Long matchId, Long loginMemberId, TeamMatchGoalCreateRequest request);
}
