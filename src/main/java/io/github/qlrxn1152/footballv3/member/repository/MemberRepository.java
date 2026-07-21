package io.github.qlrxn1152.footballv3.member.repository;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByUsername(String username);
}
