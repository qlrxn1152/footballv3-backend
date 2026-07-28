package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchPendingResponse;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchService;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchValidator;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class TeamMatchServiceImpl implements TeamMatchService {

    private final TeamMatchRepository teamMatchRepository;

    private final TeamValidator teamValidator;
    private final TeamMemberValidator teamMemberValidator;
    private final MemberValidator memberValidator;
    private final TeamMatchValidator teamMatchValidator;


    @Override
    public TeamMatchCreateResponse registerMatch(Long homeTeamId, Long loginMemberId, TeamMatchCreateRequest request) {
        Team homeTeam = teamValidator.validateExistTeamAndReturn(homeTeamId);
        Member loginMember = memberValidator.validateExistMemberAndReturn(loginMemberId);
        TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(loginMember.getId());

        teamMemberValidator.validateBelongsToTeam(homeTeam.getId(), teamMember);
        teamValidator.validateCheckTeamLeader(homeTeam, loginMember.getId());

        teamMatchValidator.validateFuturePlayedAt(request.getPlayedAt());
        teamMatchValidator.validateDuplicateRegistration(homeTeam.getId(), TeamMatchStatus.PENDING);

        TeamMatch savedTeamMatch = teamMatchRepository.save(TeamMatch.register(homeTeam, request.getPlayedAt()));

        return TeamMatchCreateResponse.of(savedTeamMatch);
    }

    @Override
    public List<TeamMatchPendingResponse> getPendingMatches() {
        // 모든 유저가 요청할 수 있음.
        return teamMatchRepository.findAllByStatusWithHomeTeam(TeamMatchStatus.PENDING)
                .stream()
                .map(TeamMatchPendingResponse::of)
                .toList();
    }


}
