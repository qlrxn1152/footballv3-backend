package io.github.qlrxn1152.footballv3.teammatch.domain;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_match_results")
public class TeamMatchResult {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_match_result_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_match_id", nullable = false, unique = true)
    private TeamMatch teamMatch;

    @Column(name = "home_score", nullable = false)
    private int homeScore;

    @Column(name = "away_score", nullable = false)
    private int awayScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    private TeamMatchResult(TeamMatch teamMatch, int homeScore, int awayScore) {
        this.teamMatch = teamMatch;
        this.homeScore = homeScore;
        this.awayScore = awayScore;

        this.winnerTeam = determineWinnerTeam(teamMatch, homeScore, awayScore);
        this.completedAt = LocalDateTime.now();
    }

    private Team determineWinnerTeam(TeamMatch teamMatch, int homeScore, int awayScore) {
        if (homeScore > awayScore) {
            return teamMatch.getHomeTeam();
        }
        else if (homeScore < awayScore) {
            return teamMatch.getAwayTeam();
        }

        return null;
    }

    public static TeamMatchResult createMatchResult(TeamMatch teamMatch, int homeScore, int awayScore) {
        return new TeamMatchResult(teamMatch, homeScore, awayScore);
    }
}
