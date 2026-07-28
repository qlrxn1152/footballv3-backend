package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMatchRepository extends JpaRepository<TeamMatch, Long> {

    boolean existsByHomeTeamIdAndStatus(Long homeTeamId, TeamMatchStatus status);

    boolean existsByAwayTeamIdAndStatus(Long awayTeamId, TeamMatchStatus status);

    @Query("select count(tm) from TeamMatch tm where tm.status = :status and (tm.homeTeam.id = :teamId or tm.awayTeam.id = :teamId)")
    long countByTeamIdAndStatus(Long teamId, TeamMatchStatus status);

    void deleteAllByHomeTeamId(Long homeTeamId);

    void deleteAllByAwayTeamId(Long awayTeamId);

    @Query("select tm from TeamMatch tm join fetch tm.homeTeam where tm.status = :status order by tm.playedAt asc, tm.createdAt asc")
    List<TeamMatch> findAllByStatusWithHomeTeam(@Param("status") TeamMatchStatus status);

    @Query("select tm from TeamMatch tm join fetch tm.homeTeam join fetch tm.awayTeam where tm.status = :status order by tm.playedAt asc, tm.createdAt asc")
    List<TeamMatch> findAllByStatusWithTeams(@Param("status") TeamMatchStatus status);

    @Query("select tm from TeamMatch tm join fetch tm.homeTeam left join fetch tm.awayTeam where tm.id = :matchId")
    Optional<TeamMatch> findByMatchIdWithTeams(@Param("matchId") Long matchId);
}
