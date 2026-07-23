package io.github.qlrxn1152.footballv3.team.validation;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.DuplicateTeamNameException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.TeamNameLengthException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamValidator {

    private final TeamRepository teamRepository;

    public void validateExistsTeamName(String teamName) {
        if (teamRepository.existsByTeamName(teamName)) {
            throw new DuplicateTeamNameException();
        }
    }

    public void validateTeamNameLength(String teamName) {
        if (teamName.length() < 2 || teamName.length() > 10) {
            throw new TeamNameLengthException();
        }
    }

    public Team validateExistTeamAndReturn(Long teamId) {
        return teamRepository.findByIdWithLeaderMember(teamId)
                .orElseThrow(NotFoundTeamException::new);
    }







}
