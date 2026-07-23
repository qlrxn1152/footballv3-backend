package io.github.qlrxn1152.footballv3.team.service;


import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamListResponse;

import java.util.List;

public interface TeamService {

    TeamCreateResponse createTeam(TeamCreateRequest request, Long memberId);

    List<TeamListResponse> getTeams();
}
