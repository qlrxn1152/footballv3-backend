package io.github.qlrxn1152.footballv3.teammatchgoal.domain;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_match_goals")
public class TeamMatchGoal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_match_goal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_match_id", nullable = false, unique = true)
    private TeamMatch teamMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "goal_count", nullable = false)
    private int goalCount;

    private TeamMatchGoal(TeamMatch teamMatch, Member member, Team team, int goalCount) {
        this.teamMatch = teamMatch;
        this.member = member;
        this.team = team;
        this.goalCount = goalCount;
    }

    public static TeamMatchGoal record(TeamMatch teamMatch, Member member, Team team, int goalCount) {
        return new TeamMatchGoal(teamMatch, member, team, goalCount);
    }

}
