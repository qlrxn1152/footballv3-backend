package io.github.qlrxn1152.footballv3.teamjoinrequest.repository;

import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    boolean existsByMemberId(Long memberId);

    @Query("select tjr from TeamJoinRequest tjr join fetch tjr.member where tjr.team.id = :teamId")
    List<TeamJoinRequest> findAllByTeamId(@Param("teamId") Long teamId);

    @Query("select tjr from TeamJoinRequest tjr join fetch tjr.member where tjr.team.id = :teamId order by tjr.requestedAt asc")
    List<TeamJoinRequest> findAllRequestOrderedRequestedAt(@Param("teamId") Long teamId);

    @Query("select tjr from TeamJoinRequest tjr join fetch tjr.member where tjr.id = :requestId")
    Optional<TeamJoinRequest> findByIdWithMember(@Param("requestId") Long requestId);

    boolean existsById(Long requestId);

}
