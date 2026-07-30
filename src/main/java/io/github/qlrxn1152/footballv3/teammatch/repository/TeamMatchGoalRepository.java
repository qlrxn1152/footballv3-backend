package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMatchGoalRepository extends JpaRepository<TeamMatchGoal, Long> {

    List<TeamMatchGoal> findAllByTeamMatchId(Long teamMatchId);

    boolean existsByTeamMatchIdAndMemberId(Long teamMatchId, Long memberId);
}
