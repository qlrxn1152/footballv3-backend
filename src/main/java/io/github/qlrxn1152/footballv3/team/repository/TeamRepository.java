package io.github.qlrxn1152.footballv3.team.repository;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);

    @Query("select t from Team t join fetch t.leaderMember order by t.createdAt desc")
    List<Team> findAllByOrderByCreatedAtDesc();

}
