package io.github.qlrxn1152.footballv3.teammatch.domain;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "team_match_goals")
public class TeamMatchGoal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_match_goal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_match_id", nullable = false)
    private TeamMatch teamMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "goal_count", nullable = false)
    private int goalCount;

    private TeamMatchGoal(TeamMatch teamMatch, Team team, Member member, int goalCount) {
        this.teamMatch = teamMatch;
        this.team = team;
        this.member = member;
        this.goalCount = goalCount;
    }

    public static TeamMatchGoal of(TeamMatch teamMatch, Team team, Member member, int goalCount) {
        return new TeamMatchGoal(teamMatch, team, member, goalCount);
    }

}
