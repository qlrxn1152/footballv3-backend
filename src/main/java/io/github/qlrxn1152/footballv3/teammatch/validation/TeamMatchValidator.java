package io.github.qlrxn1152.footballv3.teammatch.validation;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.*;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TeamMatchValidator {

    private final TeamMatchRepository teamMatchRepository;

    public void validateFuturePlayedAt(LocalDateTime playedAt) {
        if (!playedAt.isAfter(LocalDateTime.now())) {
            throw new InvalidMatchPlatedAtException();
        }
    }

    public void validateDuplicateMatchRegistration(Long teamId, TeamMatchStatus status) {
        if (teamMatchRepository.existsByHomeTeamIdAndStatus(teamId, status)) {
            throw new DuplicateMatchRegistrationException();
        }
        if (teamMatchRepository.existsByAwayTeamIdAndStatus(teamId, status)) {
            throw new DuplicateMatchRegistrationException();
        }
    }

    public TeamMatch validateExistTeamMatchAndReturn(Long matchId) {
        return teamMatchRepository.findById(matchId)
                .orElseThrow(NotFoundTeamMatchException::new);
    }

    // PENDING 인 매치가 맞는지, awayTeam, homeTeam 이 다른지
    public void validatePendingStatus(TeamMatch teamMatch) {
        if (teamMatch.getStatus() != TeamMatchStatus.PENDING) {
            throw new NotPendingTeamMatchException();
        }
    }

    public void validateDifferentTeam(TeamMatch teamMatch, Long awayTeamId) {
        if ( teamMatch.getHomeTeam().getId().equals(awayTeamId)) {
            throw new SameTeamMatchAcceptException();
        }

    }
}
