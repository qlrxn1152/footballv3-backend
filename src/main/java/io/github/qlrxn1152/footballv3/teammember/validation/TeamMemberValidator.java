package io.github.qlrxn1152.footballv3.teammember.validation;

import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotJoinedTeamException;
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

    public TeamMember validateExistTeamMemberAndReturn(Long memberId) {
        return teamMemberRepository.findByMemberId(memberId)
                .orElseThrow(NotJoinedTeamException::new);
    }



}
