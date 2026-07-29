package io.github.qlrxn1152.footballv3.team.service;


import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamNameChangeRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.*;

import java.util.List;

public interface TeamService {

    TeamCreateResponse createTeam(TeamCreateRequest request, Long memberId);

    List<TeamListResponse> getTeams();

    TeamDetailResponse getTeam(Long teamId);

    TeamLeaderTransferResponse transferTeamLeader(Long teamId, Long loginMemberId, Long newLeaderMemberId);

    void disbandTeam(Long teamId, Long loginMemberId);

    TeamNameChangeResponse changeTeamName(Long teamId, Long loginMemberId, TeamNameChangeRequest request);

    List<TeamRankingResponse> getTeamRankings();


}
