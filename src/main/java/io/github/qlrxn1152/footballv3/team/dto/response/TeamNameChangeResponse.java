package io.github.qlrxn1152.footballv3.team.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamNameChangeResponse {

    private Long teamId;
    private String oldTeamName;
    private String newTeamName;

    public static TeamNameChangeResponse of(Team team, String previousTeamName) {
        return new TeamNameChangeResponse(
                team.getId(),
                previousTeamName,
                team.getTeamName()
        );

    }
}
