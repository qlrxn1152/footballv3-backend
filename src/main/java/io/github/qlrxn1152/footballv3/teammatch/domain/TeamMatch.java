package io.github.qlrxn1152.footballv3.teammatch.domain;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "team_matches")
public class TeamMatch {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_match_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_match_status", nullable = false)
    private TeamMatchStatus status;

    @Column(name = "team_match_play_at", nullable = false)
    private LocalDateTime playedAt;

    @Column(name = "team_match_creat_at", nullable = false)
    private LocalDateTime createdAt;

    private TeamMatch(Team homeTeam, LocalDateTime playedAt) {
        this.homeTeam = homeTeam;
        this.playedAt = playedAt;

        this.awayTeam = null;
        this.status = TeamMatchStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public static TeamMatch register(Team homeTeam, LocalDateTime playedAt) {
        return new TeamMatch(homeTeam, playedAt);
    }

}
