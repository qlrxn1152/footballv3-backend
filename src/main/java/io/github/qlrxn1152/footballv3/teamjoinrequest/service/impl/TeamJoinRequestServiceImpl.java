package io.github.qlrxn1152.footballv3.teamjoinrequest.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.validation.TeamJoinRequestValidator;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
