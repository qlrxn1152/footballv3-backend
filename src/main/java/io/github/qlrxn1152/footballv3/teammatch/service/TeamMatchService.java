package io.github.qlrxn1152.footballv3.teammatch.service;

import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchAcceptResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchMatchedResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchPendingResponse;

import java.util.List;

public interface TeamMatchService {

    TeamMatchCreateResponse registerMatch(Long homeTeamId, Long loginMemberId, TeamMatchCreateRequest request);

    List<TeamMatchPendingResponse> getPendingMatches();

    List<TeamMatchMatchedResponse> getMatchedMatches();

    TeamMatchAcceptResponse acceptMatch(Long matchId, Long awayTeamId, Long loginMemberId);

}
