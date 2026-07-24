package io.github.qlrxn1152.footballv3.teamjoinrequest.validation;

import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.AlreadyRequestedTeamJoinException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamJoinRequestValidator {

    private final TeamJoinRequestRepository teamJoinRequestRepository;

    public void validateAlreadyRequested(Long memberId) {
        if (teamJoinRequestRepository.existsByMemberId(memberId)) {
            throw new AlreadyRequestedTeamJoinException();
        }
    }

}
