package io.github.qlrxn1152.footballv3.support.fixture;

import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestApproveResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamJoinFixture {

    private final TeamService teamService;
    private final TeamJoinRequestService teamJoinRequestService;

    public TeamJoinRequestApproveResponse joinTheTeam(Long teamId, Long leaderMemberId, Long newMemberId) {
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(teamId, newMemberId);
        return teamJoinRequestService.approveJoinRequest(teamId, leaderMemberId, joinRequest.getRequestId());
    }
}
