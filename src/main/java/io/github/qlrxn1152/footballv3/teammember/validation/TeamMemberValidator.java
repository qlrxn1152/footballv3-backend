package io.github.qlrxn1152.footballv3.teammember.validation;

import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamMemberValidator {

    private final TeamMemberRepository teamMemberRepository;

    public void validateAlreadyJoinedTeam(Long memberId) {
        if (teamMemberRepository.existsByMemberId(memberId)) {
            throw new AlreadyJoinedTeamException();
        }
    }



}
