package io.github.qlrxn1152.footballv3.teammember.repository;

import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByMemberId(Long memberId);

    Optional<TeamMember> findByMemberId(Long memberId);

    @Query("select tm from TeamMember tm join fetch tm.member where tm.team.id = :teamId order by tm.joinedAt asc")
    List<TeamMember> findAllByTeamIdWithMember(Long teamId); // 해당 팀에 속한 애들 다 가지고옴

}
