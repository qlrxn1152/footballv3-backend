package io.github.qlrxn1152.footballv3.support.fixture;

import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamFixture {

    private final TeamService teamService;
    private final MemberFixture memberFixture;

    public TeamCreateResponse createTeam(String teamName, Long leaderMemberId) {
        return teamService.createTeam(TeamCreateRequest.of(teamName), leaderMemberId);
    }


}
