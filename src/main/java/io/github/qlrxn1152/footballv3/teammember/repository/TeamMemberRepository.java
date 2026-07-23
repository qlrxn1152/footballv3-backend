package io.github.qlrxn1152.footballv3.teammember.repository;

import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByMemberId(Long memberId);

    Optional<TeamMember> findByMemberId(Long memberId);
}
