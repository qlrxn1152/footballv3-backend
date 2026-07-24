package io.github.qlrxn1152.footballv3.teamjoinrequest.service;

import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestListResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;

import java.util.List;

public interface TeamJoinRequestService {

    TeamJoinRequestResponse createJoinRequest(Long teamId, Long memberId);

    TeamJoinRequestListResponse getJoinRequests(Long teamId, Long leaderMemberId);


}
