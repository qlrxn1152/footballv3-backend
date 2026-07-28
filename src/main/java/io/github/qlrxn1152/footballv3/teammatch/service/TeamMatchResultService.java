package io.github.qlrxn1152.footballv3.teammatch.service;

import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchResultResponse;

public interface TeamMatchResultService {

    TeamMatchResultResponse registerMatchResult(Long matchId, Long loginMemberId, TeamMatchResultCreateRequest request);
}
