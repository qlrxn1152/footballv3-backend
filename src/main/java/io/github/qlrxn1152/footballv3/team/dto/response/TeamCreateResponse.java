package io.github.qlrxn1152.footballv3.team.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamCreateResponse {

    private Long teamId;
    private Long leaderMemberId;
    private String leaderUsername;
    private String teamName;
    private LocalDateTime createdAt;
    private TeamRole teamRole;

    public static TeamCreateResponse of(Team team, TeamRole teamRole) {
        return new TeamCreateResponse(
                team.getId(),
                team.getLeaderMember().getId(),
                team.getLeaderMember().getUsername(),
                team.getTeamName(),
                team.getCreatedAt(),
                teamRole
        );
    }


}
