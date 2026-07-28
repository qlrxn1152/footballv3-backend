package io.github.qlrxn1152.footballv3.teammatch.domain;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "team_matches")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "team_match_matched_at")
    private LocalDateTime matchedAt; // MATCHED 로 언제 변경이 된건지 확인할 수 있는

    @Column(name = "team_match_completed_at")
    private LocalDateTime completedAt;

    private TeamMatch(Team homeTeam, LocalDateTime playedAt) {
        this.homeTeam = homeTeam;
        this.playedAt = playedAt;

        this.awayTeam = null;
        this.status = TeamMatchStatus.PENDING;
        this.createdAt = LocalDateTime.now();

        this.matchedAt = null;
        this.completedAt = null;
    }

    public static TeamMatch register(Team homeTeam, LocalDateTime playedAt) {
        return new TeamMatch(homeTeam, playedAt);
    }

    public void acceptMatch(Team awayTeam) {
        this.awayTeam = awayTeam;
        this.status = TeamMatchStatus.MATCHED;
        this.matchedAt = LocalDateTime.now();
    }

    public void completeMatch() {
        this.status = TeamMatchStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void applyRating(int homeScore, int awayScore) {
        if (homeScore > awayScore) {
            this.homeTeam.winMatch();
            this.awayTeam.loseMatch();
            return;
        }

        else if ( homeScore < awayScore) {
            this.homeTeam.loseMatch();
            this.awayTeam.winMatch();
            return;
        }

        this.homeTeam.drawMatch();
        this.awayTeam.drawMatch();
    }

}
