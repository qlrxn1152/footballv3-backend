package io.github.qlrxn1152.footballv3.team.domain;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    private static final int TEAM_INIT_RATING = 1500;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "team_name", nullable = false, unique = true, length = 10)
    private String teamName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_member_id", nullable = false, unique = true)
    private Member leaderMember; // 여러팀을 생성할 수 없음. // 1명의 멤버는 1개의 팀에만 속할수있음.

    @Column(name = "team_rating", nullable = false)
    private int rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Team(String teamName, Member leaderMember) {
        this.teamName = teamName;
        this.leaderMember = leaderMember;

        this.rating = TEAM_INIT_RATING;
        this.createdAt = LocalDateTime.now();
    }

    public static Team createTeam(String teamName, Member leaderMember) {
        return new Team(teamName, leaderMember);
    }




}
