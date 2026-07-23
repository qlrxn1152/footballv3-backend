package io.github.qlrxn1152.footballv3.team.repository;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByTeamName(String teamName);
}
