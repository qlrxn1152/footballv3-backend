package io.github.qlrxn1152.footballv3.teammatchgoal.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchResultValidator;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchValidator;
import io.github.qlrxn1152.footballv3.teammatchgoal.domain.TeamMatchGoal;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalCreateRequest;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalScorerRequest;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.response.TeamMatchGoalCreateResponse;
import io.github.qlrxn1152.footballv3.teammatchgoal.repository.TeamMatchGoalRepository;
import io.github.qlrxn1152.footballv3.teammatchgoal.service.TeamMatchGoalService;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TeamMatchGoalServiceImpl implements TeamMatchGoalService {

    private final TeamMatchGoalRepository teamMatchGoalRepository;

    private final TeamValidator teamValidator;
    private final MemberValidator memberValidator;
    private final TeamMatchValidator teamMatchValidator;
    private final TeamMemberValidator teamMemberValidator;
    private final TeamMatchResultValidator teamMatchResultValidator;

    // 먼저, 경기결과를 입력 -> 점수 입력 -> 득점자 입력 -> 최종저장

    // 근데, 중간에 실패하면? 매치에 점수는 입력됐지만, 득점자를 입력하던중에 문제가생겨서 rollback?
    @Override
    public TeamMatchGoalCreateResponse registerGoals(Long matchId, Long loginMemberId, TeamMatchGoalCreateRequest request) {
        TeamMatch teamMatch = teamMatchValidator.validateExistTeamMatchAndReturnWithTeams(matchId);
        TeamMatchResult teamMatchResult = teamMatchResultValidator.validateExistTeamMatchResultAndReturnWithTeam(matchId);

        // 홈팀에서 넣은사람 누구임 ?
        for (TeamMatchGoalScorerRequest homeScorer : request.getHomeScorers()) {
            Long scorerMemberId = homeScorer.getMemberId();
            Integer scorerGoalCount = homeScorer.getGoalCount();

            Member scorer = memberValidator.validateExistMemberAndReturn(scorerMemberId);

            scorer.addGoals(scorerGoalCount);
            teamMatchGoalRepository.save(TeamMatchGoal.record(teamMatch, scorer, teamMatch.getHomeTeam(), scorerGoalCount));
        }

        // 원정팀에서 넣은사람 누구임 ?
        for (TeamMatchGoalScorerRequest awayScorer : request.getAwayScorers()) {
            Long scorerMemberId = awayScorer.getMemberId();
            Integer scorerGoalCount = awayScorer.getGoalCount();

            Member scorer = memberValidator.validateExistMemberAndReturn(scorerMemberId);

            scorer.addGoals(scorerGoalCount);
            teamMatchGoalRepository.save(TeamMatchGoal.record(teamMatch, scorer, teamMatch.getAwayTeam(), scorerGoalCount));
        }

        List<TeamMatchGoalScorerRequest> homeScorers = request.getHomeScorers();
        List<TeamMatchGoalScorerRequest> awayScorers = request.getAwayScorers();

        return TeamMatchGoalCreateResponse.of(teamMatchResult, homeScorers, awayScorers);
    }


}
