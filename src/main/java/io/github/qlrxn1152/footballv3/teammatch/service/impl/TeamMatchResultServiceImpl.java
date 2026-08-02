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
        Member loginMember = memberValidator.validateExistMemberAndReturn(loginMemberId);
        TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(loginMember.getId());

        // 해당매치가 MATCHED 인 상태가 맞는지
        teamMatchValidator.validateMatchedStatus(teamMatch);

        // 홈팀인원이 맞는지, 팀장이 맞는지
        teamMemberValidator.validateBelongsToTeam(teamMatch.getHomeTeam().getId(), teamMember);
        teamValidator.validateCheckTeamLeader(teamMatch.getHomeTeam(), loginMember.getId());

        // 이미 동일한 매치의 매치결과가 등록되어져 있는건 아닌지
        teamMatchResultValidator.validateResultNotExists(teamMatch.getId());

        // 점수방식은 올바른 방식인지
        teamMatchResultValidator.validateMatchResultScore(request.getHomeScore(), request.getAwayScore());

        // 득점 합계가 맞는지
        if ((request.getHomeScore() != request.getHomeScorers().stream().mapToInt(Scorer::getGoalCount).sum()) || (request.getAwayScore() != request.getAwayScorers().stream().mapToInt(Scorer::getGoalCount).sum())) {
            throw new InvalidTeamMatchScoreException();
        }

        validateDuplicateScorers(request);

        List<TeamMatchGoal> teamMatchGoals = getTeamMatchGoals(request, teamMatch, teamMatch.getHomeTeam().getId(), teamMatch.getAwayTeam().getId());

        teamMatchGoalRepository.saveAll(teamMatchGoals); // 득점자 정보들 저장
        TeamMatchResult matchResult = teamMatchResultRepository.save(TeamMatchResult.createMatchResult(teamMatch, request.getHomeScore(), request.getAwayScore())); // 매치 결과 저장
        teamMatch.applyRating(request.getHomeScore(), request.getAwayScore()); // Rating 반영.
        teamMatch.completeMatch();

        return TeamMatchResultResponse.of(matchResult);
    }

    public List<TeamMatchGoal> getTeamMatchGoals(TeamMatchResultCreateRequest request, TeamMatch teamMatch, Long homeTeamId, Long awayTeamId) {

        List<TeamMatchGoal> teamMatchGoals = new ArrayList<>();

        request.getHomeScorers().forEach(homeScorer -> {
            Member scorer = memberValidator.validateExistMemberAndReturn(homeScorer.getMemberId());
            TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(scorer.getId());
            teamMemberValidator.validateBelongsToTeam(homeTeamId, teamMember);


            TeamMatchGoal teamMatchGoal = TeamMatchGoal.of(teamMatch, teamMatch.getHomeTeam(), scorer, homeScorer.getGoalCount());
            scorer.addGoals(homeScorer.getGoalCount());
            teamMatchGoals.add(teamMatchGoal);
        });

        request.getAwayScorers().forEach(awayScorer -> {
            Member scorer = memberValidator.validateExistMemberAndReturn(awayScorer.getMemberId());
            TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(scorer.getId());
            teamMemberValidator.validateBelongsToTeam(awayTeamId, teamMember);

            TeamMatchGoal teamMatchGoal = TeamMatchGoal.of(teamMatch, teamMatch.getAwayTeam(), scorer, awayScorer.getGoalCount());
            scorer.addGoals(awayScorer.getGoalCount());
            teamMatchGoals.add(teamMatchGoal);
        });

        return teamMatchGoals;
    }

    public void validateDuplicateScorers(TeamMatchResultCreateRequest request) {
        List<Long> memberIds = new ArrayList<>();

        request.getHomeScorers().forEach(scorer -> memberIds.add(scorer.getMemberId()));
        request.getAwayScorers().forEach(scorer -> memberIds.add(scorer.getMemberId()));

        Set<Long> uniqueMemberIds = new HashSet<>(memberIds);

        if (memberIds.size() != uniqueMemberIds.size()) {
            throw new DuplicateTeamMatchGoalScorerException();
        }
    }


}
