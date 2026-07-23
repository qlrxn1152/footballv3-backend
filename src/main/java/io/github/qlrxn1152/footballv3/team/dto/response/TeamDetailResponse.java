package io.github.qlrxn1152.footballv3.team.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamDetailResponse {

    private Long teamId;
    private String teamName;
    private int teamRating;
    private LocalDateTime createdAt;

    private Long leaderMemberId;
    private String leaderUsername;

    private int memberCount;

    private List<TeamMemberResponse> members;

    public static TeamDetailResponse of(Team team, List<TeamMemberResponse> members) {
        return new TeamDetailResponse(
                team.getId(),
                team.getTeamName(),
                team.getRating(),
                team.getCreatedAt(),

                team.getLeaderMember().getId(),
                team.getLeaderMember().getUsername(), // leaderMember 필요

                members.size(),

                members
        );

    }
}
