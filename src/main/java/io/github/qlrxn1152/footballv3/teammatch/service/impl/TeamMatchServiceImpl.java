package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchGoal;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.*;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchGoalRepository;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchService;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchResultValidator;
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
    private final TeamMatchResultRepository teamMatchResultRepository;
    private final TeamMatchGoalRepository teamMatchGoalRepository;

    private final TeamValidator teamValidator;
    private final TeamMemberValidator teamMemberValidator;
    private final MemberValidator memberValidator;
    private final TeamMatchValidator teamMatchValidator;
    private final TeamMatchResultValidator teamMatchResultValidator;

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
    @Transactional(readOnly = true)
    public List<TeamMatchPendingResponse> getPendingMatches() {
        // 모든 유저가 요청할 수 있음.
        return teamMatchRepository.findAllByStatusWithHomeTeam(TeamMatchStatus.PENDING)
                .stream()
                .map(TeamMatchPendingResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMatchMatchedResponse> getMatchedMatches() {
        return teamMatchRepository.findAllByStatusWithTeams(TeamMatchStatus.MATCHED)
                .stream()
                .map(TeamMatchMatchedResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMatchCompletedResponse> getCompletedMatches() {
        return teamMatchResultRepository.findAllCompletedMatchWithMatchAndTeams(TeamMatchStatus.COMPLETED)
                .stream()
                .map(TeamMatchCompletedResponse::of)
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

    @Override
    @Transactional(readOnly = true)
    public List<TeamMatchPendingResponse> getPendingMatchesByTeamId(Long teamId) {
        teamValidator.validateExistTeamAndReturn(teamId);

        return teamMatchRepository.findAllPendingByTeamIdWithHomeTeam(teamId, TeamMatchStatus.PENDING)
                .stream()
                .map(TeamMatchPendingResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMatchMatchedResponse> getMatchedMatchesByTeamId(Long teamId) {
        teamValidator.validateExistTeamAndReturn(teamId);
        return teamMatchRepository.findAllMatchedByTeamIdWithHomeTeam(teamId, TeamMatchStatus.MATCHED)
                .stream()
                .map(TeamMatchMatchedResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMatchCompletedResponse> getCompletedMatchesByTeamId(Long teamId) {
        teamValidator.validateExistTeamAndReturn(teamId);
        return teamMatchResultRepository.findAllCompletedByTeamIdWithMatchAndTeams(teamId, TeamMatchStatus.COMPLETED)
                .stream()
                .map(TeamMatchCompletedResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamMatchDetailResponse getMatch(Long matchId) {
        TeamMatch teamMatch = teamMatchValidator.validateExistTeamMatchAndReturnWithTeams(matchId);
        TeamMatchResult teamMatchResult = null;
        List<TeamMatchGoal> teamMatchGoals = List.of();

        // COMPLETED 인 경우에만, MatchResult 가 존재하므로.
        if (teamMatch.getStatus() == TeamMatchStatus.COMPLETED) {
            teamMatchResult = teamMatchResultValidator.validateExistTeamMatchResultAndReturnWithTeam(matchId);
            teamMatchGoals = teamMatchGoalRepository.findAllByMatchIdWithMemberAndTeam(matchId);
        }


        return TeamMatchDetailResponse.of(teamMatch, teamMatchResult, teamMatchGoals);
    }


}
