package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMatchResultRepository extends JpaRepository<TeamMatchResult, Long> {

    boolean existsByTeamMatchId(Long matchId);

    Optional<TeamMatchResult> findByTeamMatchId(Long matchId);

    @Query("select tmr from TeamMatchResult tmr join fetch tmr.teamMatch tm join fetch tm.homeTeam join fetch tm.awayTeam left join fetch tmr.winnerTeam where tm.status = :status order by tm.completedAt desc, tm.id desc")
    List<TeamMatchResult> findAllCompletedMatchWithMatchAndTeams(@Param("status") TeamMatchStatus status);

    @Query("select tmr from TeamMatchResult tmr join fetch tmr.teamMatch tm join fetch tm.homeTeam join fetch tm.awayTeam left join fetch tmr.winnerTeam where tm.status = :status and (tm.homeTeam.id = :teamId or tm.awayTeam.id = :teamId)")
    List<TeamMatchResult> findAllCompletedByTeamIdWithMatchAndTeams(@Param("teamId") Long teamId, @Param("status") TeamMatchStatus status);

    @Query("select tmr from TeamMatchResult tmr left join fetch tmr.winnerTeam where tmr.teamMatch.id = :matchId")
    Optional<TeamMatchResult> findByTeamMatchIdWithWinnerTeam(@Param("matchId") Long matchId);



}
