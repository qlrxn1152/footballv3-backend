package io.github.qlrxn1152.footballv3.teammatch.validation;

import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.AlreadyExistTeamMatchResultException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.InvalidTeamMatchScoreException;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
