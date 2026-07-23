package io.github.qlrxn1152.footballv3.team.service;


import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;

public interface TeamService {

    TeamCreateResponse createTeam(TeamCreateRequest request, Long memberId);
}
