package io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamJoinRequestApproveResponse {

    private Long teamId;

    private Long memberId;
    private String memberUsername;

    private TeamRole teamRole;
    private LocalDateTime joinedAt;

    public static TeamJoinRequestApproveResponse of(TeamMember teamMember) {
        return new TeamJoinRequestApproveResponse(
                teamMember.getTeam().getId(),
                teamMember.getMember().getId(),
                teamMember.getMember().getUsername(),
                teamMember.getRole(),
                teamMember.getJoinedAt()
        );
    }
}
