package io.github.qlrxn1152.footballv3.teammember.service.impl;

import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import io.github.qlrxn1152.footballv3.teammember.service.TeamMemberService;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeamMemberServiceImpl implements TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;

    private final TeamMemberValidator teamMemberValidator;
    private final TeamValidator teamValidator;
    private final MemberValidator memberValidator;

    @Override
    public void leaveTeam(Long teamId, Long memberId) {
        teamValidator.validateExistTeamAndReturn(teamId);
        memberValidator.validateExistMemberAndReturn(memberId);
        TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(memberId);

        teamMemberValidator.validateBelongsToTeam(teamId, teamMember);
        teamMemberValidator.validateCanLeaveTeam(teamMember);

        teamMemberRepository.delete(teamMember);
    }





}
