package io.github.qlrxn1152.footballv3.teamjoinrequest.repository;

import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    boolean existsByMemberId(Long memberId);
}
