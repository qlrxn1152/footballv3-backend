package io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response;


import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamJoinRequestListResponse {

    private Long teamId;
    private String teamName;

    private int requestCount;

    private List<TeamJoinRequestMemberResponse> requests;

    public static TeamJoinRequestListResponse of(Team team, List<TeamJoinRequestMemberResponse> requests) {
        return new TeamJoinRequestListResponse(
                team.getId(),
                team.getTeamName(),
                requests.size(),
                requests
        );
    }
}
