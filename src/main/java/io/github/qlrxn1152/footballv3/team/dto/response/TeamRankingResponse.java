package io.github.qlrxn1152.footballv3.team.dto.response;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamRankingResponse {

    private int rank;

    private Long teamId;
    private String teamName;
    private int rating;

    private Long leaderMemberId;
    private String leaderUsername;

    public static TeamRankingResponse of(int rank, Team team) {
        return new TeamRankingResponse(
                rank,
                team.getId(),
                team.getTeamName(),
                team.getRating(),
                team.getLeaderMember().getId(),
                team.getLeaderMember().getUsername()
        );
    }
}
