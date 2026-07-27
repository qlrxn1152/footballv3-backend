package io.github.qlrxn1152.footballv3.team.dto.response;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamLeaderTransferResponse {

    private Long teamId;
    private String teamName;

    private Long oldLeaderMemberId;
    private String oldLeaderUsername;

    private Long newLeaderMemberId;
    private String newLeaderUsername;

    public static TeamLeaderTransferResponse of(Team team, Member oldLeaderMember, Member newLeaderMember) {
        return new TeamLeaderTransferResponse(
                team.getId(),
                team.getTeamName(),
                oldLeaderMember.getId(),
                oldLeaderMember.getUsername(),
                newLeaderMember.getId(),
                newLeaderMember.getUsername()
        );
    }

}
