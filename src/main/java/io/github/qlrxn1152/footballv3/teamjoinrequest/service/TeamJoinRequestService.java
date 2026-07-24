package io.github.qlrxn1152.footballv3.teamjoinrequest.service;

import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;

public interface TeamJoinRequestService {

    TeamJoinRequestResponse createJoinRequest(Long teamId, Long memberId);


}
