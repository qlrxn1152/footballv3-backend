package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchGoal;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchResultResponse;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.DuplicateTeamMatchGoalScorerException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.InvalidTeamMatchScoreException;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchGoalRepository;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchResultService;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchResultValidator;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchValidator;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TeamMatchResultServiceImpl implements TeamMatchResultService {

    private final TeamMatchResultRepository teamMatchResultRepository;
    private final TeamMatchGoalRepository teamMatchGoalRepository;

    private final TeamValidator teamValidator;
    private final MemberValidator memberValidator;
    private final TeamMemberValidator teamMemberValidator;
    private final TeamMatchValidator teamMatchValidator;
    private final TeamMatchResultValidator teamMatchResultValidator;

    @Override
    public TeamMatchResultResponse registerMatchResult(Long matchId, Long loginMemberId, TeamMatchResultCreateRequest request) {
        TeamMatch teamMatch = teamMatchValidator.validateExistTeamMatchAndReturnWithTeams(matchId); // 매치조회

        validateRegisterAuthority(teamMatch, loginMemberId);
        validateResultRequest(teamMatch, request);

        List<TeamMatchGoal> teamMatchGoals = createGoals(request, teamMatch);

        teamMatchGoalRepository.saveAll(teamMatchGoals); // 득점자 정보들 저장
        TeamMatchResult matchResult = teamMatchResultRepository.save(TeamMatchResult.createMatchResult(teamMatch, request.getHomeScore(), request.getAwayScore())); // 매치 결과 저장
        teamMatch.completeMatch(request.getHomeScore(), request.getAwayScore());

        return TeamMatchResultResponse.of(matchResult);
    }



    // ========== 검증로직 =========
    private void validateResultRequest(TeamMatch teamMatch, TeamMatchResultCreateRequest request) {
        teamMatchResultValidator.validateResultNotExists(teamMatch.getId());
        teamMatchResultValidator.validateMatchResultScore(request.getHomeScore(), request.getAwayScore());
        teamMatchResultValidator.validateDuplicateScorers(request);
        teamMatchResultValidator.validateTotalScore(request);
    }

    private void validateRegisterAuthority(TeamMatch teamMatch, Long loginMemberId) {
        Member loginMember = memberValidator.validateExistMemberAndReturn(loginMemberId);
        TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(loginMemberId);

        teamMatchValidator.validateMatchedStatus(teamMatch);
        teamMemberValidator.validateBelongsToTeam(teamMatch.getHomeTeam().getId(), teamMember);
        teamValidator.validateCheckTeamLeader(teamMatch.getHomeTeam(), loginMember.getId());
    }


    // ===== 비즈니스로직 ====== //
    private List<TeamMatchGoal> createGoals(TeamMatchResultCreateRequest request, TeamMatch teamMatch) {
        List<TeamMatchGoal> teamGoals = new ArrayList<>();

        List<Scorer> homeScorers = request.getHomeScorers();
        List<Scorer> awayScorers = request.getAwayScorers();

        teamGoals.addAll(createTeamGoals(teamMatch, homeScorers, teamMatch.getHomeTeam()));
        teamGoals.addAll(createTeamGoals(teamMatch, awayScorers, teamMatch.getAwayTeam()));

        return teamGoals;
    }


    private List<TeamMatchGoal> createTeamGoals(TeamMatch teamMatch, List<Scorer> scorers, Team team) {

        List<TeamMatchGoal> teamMatchGoals = new ArrayList<>();

        scorers.forEach(homeScorer -> {
            Member scorer = memberValidator.validateExistMemberAndReturn(homeScorer.getMemberId());
            TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(scorer.getId());
            teamMemberValidator.validateBelongsToTeam(team.getId(), teamMember); //

            TeamMatchGoal teamMatchGoal = TeamMatchGoal.of(teamMatch, team, scorer, homeScorer.getGoalCount());
            scorer.addGoals(homeScorer.getGoalCount());
            teamMatchGoals.add(teamMatchGoal);
        });

        return teamMatchGoals;
    }



}
