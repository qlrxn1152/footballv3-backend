package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchAcceptResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchMatchedResponse;
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
        teamMatchValidator.validateDuplicateMatchRegistration(homeTeam.getId(), TeamMatchStatus.PENDING); // PENDING 인 매치들이 이미 존재하는지 확인.

        // MATCHED 를 가지고있어도, PENDING 매치는 등록할 수 있다.
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

    @Override
    public List<TeamMatchMatchedResponse> getMatchedMatches() {
        return teamMatchRepository.findAllByStatusWithTeams(TeamMatchStatus.MATCHED)
                .stream()
                .map(TeamMatchMatchedResponse::of)
                .toList();
    }

    @Override
    public TeamMatchAcceptResponse acceptMatch(Long matchId, Long awayTeamId, Long loginMemberId) {
        TeamMatch teamMatch = teamMatchValidator.validateExistTeamMatchAndReturn(matchId);
        Team awayTeam = teamValidator.validateExistTeamAndReturn(awayTeamId);
        Member loginMember = memberValidator.validateExistMemberAndReturn(loginMemberId);

        TeamMember loginTeamMember = teamMemberValidator.validateExistTeamMemberAndReturn(loginMember.getId());
        teamMemberValidator.validateBelongsToTeam(awayTeam.getId(), loginTeamMember);
        teamValidator.validateCheckTeamLeader(awayTeam, loginMember.getId());
        teamMatchValidator.validateDifferentTeam(teamMatch, awayTeam.getId());
        teamMatchValidator.validatePendingStatus(teamMatch);

        teamMatch.acceptMatch(awayTeam);

        return TeamMatchAcceptResponse.of(teamMatch);


    }


}
