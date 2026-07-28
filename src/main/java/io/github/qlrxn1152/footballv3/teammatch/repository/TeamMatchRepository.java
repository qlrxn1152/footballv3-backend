package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMatchRepository extends JpaRepository<TeamMatch, Long> {

    boolean existsByHomeTeamIdAndStatus(Long homeTeamId, TeamMatchStatus status);

    long countByHomeTeamIdAndStatus(Long homeTeamId, TeamMatchStatus status);

    void deleteAllByHomeTeamId(Long homeTeamId);
}
