package io.github.qlrxn1152.footballv3.teamjoinrequest.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestListResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestMemberResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.validation.TeamJoinRequestValidator;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeamJoinRequestServiceImpl implements TeamJoinRequestService {

    private final TeamJoinRequestRepository teamJoinRequestRepository;

    private final TeamValidator teamValidator;
    private final TeamMemberValidator teamMemberValidator;
    private final MemberValidator memberValidator;
    private final TeamJoinRequestValidator teamJoinRequestValidator;


    @Override
    public TeamJoinRequestResponse createJoinRequest(Long teamId, Long memberId) {
        Team team = teamValidator.validateExistTeamAndReturn(teamId);
        Member member = memberValidator.validateExistMemberAndReturn(memberId);
        teamMemberValidator.validateAlreadyJoinedTeam(memberId);
        teamJoinRequestValidator.validateAlreadyRequested(memberId);

        TeamJoinRequest savedRequest = teamJoinRequestRepository.save(TeamJoinRequest.create(team, member));

        return TeamJoinRequestResponse.of(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamJoinRequestListResponse getJoinRequests(Long teamId, Long memberId) {

        Team team = teamValidator.validateExistTeamAndReturnWithLeaderMember(teamId); // 쿼리 1번

        Member member = memberValidator.validateExistMemberAndReturn(memberId); // 영속성 캐쉬에 있는거로 확인 ( team -> leaderMember ) -> 없으면 다시조회 ..
        teamValidator.validateCheckTeamLeader(team, member.getId()); // 영속성 캐쉬에 있는거로 확인 ( team 이 존재)

        // 가입신청이 오래된 순서로 정렬 ...
        List<TeamJoinRequestMemberResponse> requests = teamJoinRequestRepository.findAllRequestOrderedRequestedAt(team.getId()) // 쿼리 1번
                .stream()
                .map(TeamJoinRequestMemberResponse::of)
                .toList();

        return TeamJoinRequestListResponse.of(team, requests);
    }
}
