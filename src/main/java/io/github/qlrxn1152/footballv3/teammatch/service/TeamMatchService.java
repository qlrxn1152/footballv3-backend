package io.github.qlrxn1152.footballv3.teammatch.service;

import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;

public interface TeamMatchService {

    TeamMatchCreateResponse registerMatch(Long homeTeamId, Long loginMemberId, TeamMatchCreateRequest request);
}
