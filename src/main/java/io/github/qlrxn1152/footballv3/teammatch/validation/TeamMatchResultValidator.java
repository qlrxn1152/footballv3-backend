package io.github.qlrxn1152.footballv3.teammatch.validation;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.AlreadyExistTeamMatchResultException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.DuplicateTeamMatchGoalScorerException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.InvalidTeamMatchScoreException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.NotFoundTeamMatchException;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest.*;

@Component
@RequiredArgsConstructor
public class TeamMatchResultValidator {

    private final TeamMatchResultRepository teamMatchResultRepository;

    public void validateMatchResultScore(Integer homeScore, Integer awayScore) {
        if ( homeScore == null || awayScore == null || homeScore < 0 || awayScore < 0) {
            throw new InvalidTeamMatchScoreException();
        }
    }

    public void validateResultNotExists(Long matchId) {
        if ( teamMatchResultRepository.existsByTeamMatchId(matchId) ) {
            throw new AlreadyExistTeamMatchResultException();
        }
    }

    public TeamMatchResult validateExistTeamMatchResultAndReturnWithTeam(Long matchId) {
        return teamMatchResultRepository.findByTeamMatchIdWithWinnerTeam(matchId)
                .orElseThrow(NotFoundTeamMatchException::new);
    }

    public void validateTotalScore(TeamMatchResultCreateRequest request) {
        Integer matchHomeScore = request.getHomeScore();
        Integer matchAwayScore = request.getAwayScore();

        int requestHomeScore = request.getHomeScorers().stream().mapToInt(Scorer::getGoalCount).sum();
        int requestAwayScore = request.getHomeScorers().stream().mapToInt(Scorer::getGoalCount).sum();

        if ((!matchHomeScore.equals(requestHomeScore) || (!matchAwayScore.equals(requestAwayScore)))) {
            throw new InvalidTeamMatchScoreException();
        }
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
