package io.github.qlrxn1152.footballv3.teammatch.validation;

import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.DuplicateMatchRegistrationException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.InvalidMatchPlatedAtException;
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

    public void validateDuplicateRegistration(Long homeTeamId, TeamMatchStatus status) {
        if (teamMatchRepository.existsByHomeTeamIdAndStatus(homeTeamId, status)) {
            throw new DuplicateMatchRegistrationException();
        }

    }


}
