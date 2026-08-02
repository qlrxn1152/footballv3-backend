package io.github.qlrxn1152.footballv3.teammatch.repository;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMatchGoalRepository extends JpaRepository<TeamMatchGoal, Long> {

    List<TeamMatchGoal> findAllByTeamMatchId(Long teamMatchId);

    boolean existsByTeamMatchIdAndMemberId(Long teamMatchId, Long memberId);

    @Query("select tmg from TeamMatchGoal tmg join fetch tmg.team join fetch tmg.member where tmg.teamMatch.id = :matchId")
    List<TeamMatchGoal> findAllByMatchIdWithMemberAndTeam(@Param("matchId") Long matchId);
}
