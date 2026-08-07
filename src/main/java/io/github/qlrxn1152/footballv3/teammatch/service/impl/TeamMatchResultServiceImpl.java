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
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchGoalRepository;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchResultService;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchResultValidator;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchValidator;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

        validateAuthorize(loginMemberId, teamMatch);
        validateCanRegisterMatchResult(request, teamMatch);

        createGoalsAndMatchGoalsSave(request, teamMatch);
        TeamMatchResult matchResult = registerMatchResultAndComplete(request, teamMatch);

        return TeamMatchResultResponse.of(matchResult);
    }




















    // ========== 검증로직 =========

    private void validateAuthorize(Long loginMemberId, TeamMatch teamMatch) {
        Member loginMember = memberValidator.validateExistMemberAndReturn(loginMemberId);
        TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(loginMemberId);
        teamMemberValidator.validateBelongsToTeam(teamMatch.getHomeTeam().getId(), teamMember);
        teamValidator.validateCheckTeamLeader(teamMatch.getHomeTeam(), loginMember.getId());
    }

    private void validateCanRegisterMatchResult(TeamMatchResultCreateRequest request, TeamMatch teamMatch) {
        teamMatchValidator.validateMatchedStatus(teamMatch);
        teamMatchResultValidator.validateResultNotExists(teamMatch.getId());
        teamMatchResultValidator.validateMatchResultScore(request.getHomeScore(), request.getAwayScore());
        teamMatchResultValidator.validateDuplicateScorers(request);
        teamMatchResultValidator.validateTotalScore(request);
    }

    // ===== 비즈니스로직 ====== //

    private void createGoalsAndMatchGoalsSave(TeamMatchResultCreateRequest request, TeamMatch teamMatch) {
        List<TeamMatchGoal> teamMatchGoals = new ArrayList<>();

        createGoals(request.getHomeScorers(), teamMatchGoals, teamMatch, teamMatch.getHomeTeam());
        createGoals(request.getAwayScorers(), teamMatchGoals, teamMatch, teamMatch.getAwayTeam());

        teamMatchGoalRepository.saveAll(teamMatchGoals); // 득점자 정보들 저장 --> 해당 매치의 득점자들에 대한 정보 ( 어떤매치에, 어느팀, 누가, 몇골 )
    }

    private void createGoals(List<Scorer> scorers, List<TeamMatchGoal> teamMatchGoals, TeamMatch teamMatch, Team team) {
        scorers.forEach(scorer -> createGoal(scorer, teamMatchGoals, teamMatch, team));
    }

    private void createGoal(Scorer scorer, List<TeamMatchGoal> teamMatchGoals, TeamMatch teamMatch, Team teamMatch1) {
        Member member = memberValidator.validateExistMemberAndReturn(scorer.getMemberId());
        member.addGoals(scorer.getGoalCount());
        teamMatchGoals.add(TeamMatchGoal.of(teamMatch, teamMatch1, member, scorer.getGoalCount()));
    }

    private @NonNull TeamMatchResult registerMatchResultAndComplete(TeamMatchResultCreateRequest request, TeamMatch teamMatch) {
        TeamMatchResult matchResult = teamMatchResultRepository.save(TeamMatchResult.createMatchResult(teamMatch, request.getHomeScore(), request.getAwayScore())); // 매치 결과 저장
        teamMatch.completeMatch(request.getHomeScore(), request.getAwayScore());
        return matchResult;
    }


}
