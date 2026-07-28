package io.github.qlrxn1152.footballv3.team.validation;

import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.*;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamValidator {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMatchRepository teamMatchRepository;

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

    public Team validateExistTeamAndReturnWithLeaderMember(Long teamId) {
        return teamRepository.findByIdWithLeaderMember(teamId)
                .orElseThrow(NotFoundTeamException::new);
    }

    public Team validateExistTeamAndReturn(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(NotFoundTeamException::new);
    }

    public void validateCheckTeamLeader(Team team, Long memberId) {
        if (!team.getLeaderMember().getId().equals(memberId)) {
            throw new NotTeamLeaderException();
        }
    }

    public void validateCheckSelfAppoint(Long oldLeaderMemberId, Long newLeaderMemberId) {
        if (oldLeaderMemberId.equals(newLeaderMemberId)) {
            throw new SameTeamLeaderException();
        }
    }

    public void validateCanDisbandTeam(Long teamId) {
        List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamIdWithMember(teamId);

        if (teamMembers.size() != 1) {
            throw new CanNotDisbandTeamException();
        }

    }

    public void validateSameTeamName(String oldTeamName, String newTeamName) {
        if (oldTeamName.equals(newTeamName)) {
            throw new SameTeamNameException();
        }
    }

    // TODO : EXIST ? COUNT ?
    public void validateCanDisBandMatchedTeam(Long teamId) {
        long matchedMatchCount = teamMatchRepository.countByTeamIdAndStatus(teamId, TeamMatchStatus.MATCHED);

        if ( matchedMatchCount > 0 ) {
            throw new CanNotDisbandMatchedTeamException();
        }

    }





}
