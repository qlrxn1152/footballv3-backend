package io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response;

import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamJoinRequestResponse {

    private Long requestId;

    private Long teamId;
    private String teamName;

    private Long memberId;
    private String memberUsername;

    private LocalDateTime requestedAt;

    public static TeamJoinRequestResponse of(TeamJoinRequest teamJoinRequest) {
        return new TeamJoinRequestResponse(
                teamJoinRequest.getId(),
                teamJoinRequest.getTeam().getId(),
                teamJoinRequest.getTeam().getTeamName(),
                teamJoinRequest.getMember().getId(),
                teamJoinRequest.getMember().getUsername(),
                teamJoinRequest.getRequestedAt()
        );
    }
}
