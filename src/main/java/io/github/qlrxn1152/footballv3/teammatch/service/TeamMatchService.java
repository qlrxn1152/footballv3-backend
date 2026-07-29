package io.github.qlrxn1152.footballv3.teammatch.service;

import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.*;

import java.util.List;

public interface TeamMatchService {

    TeamMatchCreateResponse registerMatch(Long homeTeamId, Long loginMemberId, TeamMatchCreateRequest request);

    List<TeamMatchPendingResponse> getPendingMatches();

    List<TeamMatchMatchedResponse> getMatchedMatches();

    List<TeamMatchCompletedResponse> getCompletedMatches();

    TeamMatchAcceptResponse acceptMatch(Long matchId, Long awayTeamId, Long loginMemberId);

    List<TeamMatchPendingResponse> getPendingMatchesByTeamId(Long teamId);

    List<TeamMatchMatchedResponse> getMatchedMatchesByTeamId(Long teamId);

    List<TeamMatchCompletedResponse> getCompletedMatchesByTeamId(Long teamId);

}
