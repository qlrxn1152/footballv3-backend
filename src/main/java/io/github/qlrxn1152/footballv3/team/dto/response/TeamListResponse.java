package io.github.qlrxn1152.footballv3.team.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamListResponse {

    private Long teamId;
    private String teamName;
    private Long leaderMemberId;
    private String leaderUsername;
    private int rating;
    private LocalDateTime createdAt;

    public static TeamListResponse of(Team team) {
        return new TeamListResponse(
                team.getId(),
                team.getTeamName(),
                team.getLeaderMember().getId(),
                team.getLeaderMember().getUsername(),
                team.getRating(),
                team.getCreatedAt()
        );
    }
}
