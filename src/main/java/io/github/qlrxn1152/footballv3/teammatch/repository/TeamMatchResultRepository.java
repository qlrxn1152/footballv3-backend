package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMatchResultRepository extends JpaRepository<TeamMatchResult, Long> {

    boolean existsByTeamMatchId(Long matchId);

    Optional<TeamMatchResult> findByTeamMatchId(Long matchId);
}
