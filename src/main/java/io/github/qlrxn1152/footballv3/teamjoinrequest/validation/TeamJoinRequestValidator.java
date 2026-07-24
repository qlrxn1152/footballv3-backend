package io.github.qlrxn1152.footballv3.teamjoinrequest.validation;

import io.github.qlrxn1152.footballv3.teamjoinrequest.domain.TeamJoinRequest;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.AlreadyRequestedTeamJoinException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotFoundTeamJoinRequestException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
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

    public TeamJoinRequest validateExistTeamJoinRequestAndReturnWithMember(Long requestId) {
        return teamJoinRequestRepository.findByIdWithMember(requestId)
                .orElseThrow(NotFoundTeamJoinRequestException::new);
    }

    public void validateSameTeam(TeamJoinRequest joinRequest, Long teamId) {
        if (!joinRequest.getTeam().getId().equals(teamId)) {
            throw new NotSameTeamException();
        }
    }



}
