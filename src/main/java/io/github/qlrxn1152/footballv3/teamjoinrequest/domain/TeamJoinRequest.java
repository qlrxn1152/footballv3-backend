package io.github.qlrxn1152.footballv3.teamjoinrequest.domain;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_join_requests")
public class TeamJoinRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_join_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    private TeamJoinRequest(Team team, Member member) {
        this.team = team;
        this.member = member;
        this.requestedAt = LocalDateTime.now();
    }

    public static TeamJoinRequest create(Team team, Member member) {
        return new TeamJoinRequest(team, member);
    }

}
