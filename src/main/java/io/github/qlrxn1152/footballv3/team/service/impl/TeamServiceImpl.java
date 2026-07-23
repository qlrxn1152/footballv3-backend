package io.github.qlrxn1152.footballv3.team.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.DuplicateTeamNameException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.TeamNameLengthException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    private final MemberValidator memberValidator;
    private final TeamValidator teamValidator;
    private final TeamMemberValidator teamMemberValidator;

    @Override
    public TeamCreateResponse createTeam(TeamCreateRequest request, Long memberId) {
        String normalizedTeamName = request.getTeamName().strip();

        teamValidator.validateExistsTeamName(normalizedTeamName);
        teamValidator.validateTeamNameLength(normalizedTeamName);
        Member member = memberValidator.validateExistMember(memberId);
        teamMemberValidator.validateAlreadyJoinedTeam(memberId);

        Team savedTeam = teamRepository.save(Team.createTeam(normalizedTeamName, member));
        TeamMember savedTeamMember = teamMemberRepository.save(TeamMember.createTeam(savedTeam, member));

        return TeamCreateResponse.of(savedTeam, savedTeamMember.getRole());
    }


}
