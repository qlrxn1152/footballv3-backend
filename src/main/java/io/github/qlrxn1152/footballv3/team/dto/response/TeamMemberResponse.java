package io.github.qlrxn1152.footballv3.team.dto.response;

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
public class TeamMemberResponse {

    private Long memberId;
    private String username;
    private int memberRating;
    private TeamRole teamRole;
    private LocalDateTime joinedAt;

    public static TeamMemberResponse of(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getMember().getId(),
                teamMember.getMember().getUsername(),
                teamMember.getMember().getRating(),
                teamMember.getRole(),
                teamMember.getJoinedAt()
        );
    }

}
