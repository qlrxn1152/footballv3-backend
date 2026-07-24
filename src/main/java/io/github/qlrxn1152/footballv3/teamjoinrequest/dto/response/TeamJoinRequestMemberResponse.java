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
public class TeamJoinRequestMemberResponse {

    private Long requestId;

    private Long memberId;
    private String memberUsername;
    private int memberRating;

    private LocalDateTime requestedAt;

    public static TeamJoinRequestMemberResponse of(TeamJoinRequest teamJoinRequest) {
        return new TeamJoinRequestMemberResponse(
                teamJoinRequest.getId(),
                teamJoinRequest.getMember().getId(),
                teamJoinRequest.getMember().getUsername(),
                teamJoinRequest.getMember().getRating(),
                teamJoinRequest.getRequestedAt()
        );
    }
}
