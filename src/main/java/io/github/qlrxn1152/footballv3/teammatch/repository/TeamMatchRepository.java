package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamMatchRepository extends JpaRepository<TeamMatch, Long> {

    boolean existsByHomeTeamIdAndStatus(Long homeTeamId, TeamMatchStatus status);

    long countByHomeTeamIdAndStatus(Long homeTeamId, TeamMatchStatus status);

    void deleteAllByHomeTeamId(Long homeTeamId);

    @Query("select tm from TeamMatch tm join fetch tm.homeTeam where tm.status = :status order by tm.playedAt asc, tm.createdAt asc")
    List<TeamMatch> findAllByStatusWithHomeTeam(@Param("status") TeamMatchStatus status);

}
