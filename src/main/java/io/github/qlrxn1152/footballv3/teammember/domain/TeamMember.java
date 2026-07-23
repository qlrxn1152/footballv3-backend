package io.github.qlrxn1152.footballv3.teammember.domain;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_role", nullable = false)
    private TeamRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    private TeamMember(Team team, Member member, TeamRole role) {
        this.team = team;
        this.member = member;
        this.role = role;

        this.joinedAt = LocalDateTime.now();
    }






    public static TeamMember createTeam(Team team, Member member) {
        return new TeamMember(team, member, TeamRole.LEADER);
    }

    public static TeamMember joinTeam(Team team, Member member) {
        return new TeamMember(team, member, TeamRole.MEMBER);
    }




}
