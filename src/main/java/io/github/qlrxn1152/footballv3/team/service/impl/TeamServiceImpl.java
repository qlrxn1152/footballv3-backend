package io.github.qlrxn1152.footballv3.team.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamNameChangeRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.*;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.CanNotDisbandTeamException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.SameTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.team.validation.TeamValidator;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammatch.validation.TeamMatchValidator;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import io.github.qlrxn1152.footballv3.teammember.validation.TeamMemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamJoinRequestRepository teamJoinRequestRepository;
    private final TeamMatchRepository teamMatchRepository;

    private final MemberValidator memberValidator;
    private final TeamValidator teamValidator;
    private final TeamMemberValidator teamMemberValidator;
    private final TeamMatchValidator teamMatchValidator;

    @Override
    public TeamCreateResponse createTeam(TeamCreateRequest request, Long memberId) {
        String normalizedTeamName = request.getTeamName().strip();
        teamMemberValidator.validateAlreadyJoinedTeam(memberId);

        teamValidator.validateExistsTeamName(normalizedTeamName);
        teamValidator.validateTeamNameLength(normalizedTeamName);
        Member member = memberValidator.validateExistMemberAndReturn(memberId);

        Team savedTeam = teamRepository.save(Team.createTeam(normalizedTeamName, member));
        TeamMember savedTeamMember = teamMemberRepository.save(TeamMember.createTeam(savedTeam, member));

        return TeamCreateResponse.of(savedTeam, savedTeamMember.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamListResponse> getTeams() {
        return teamRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TeamListResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDetailResponse getTeam(Long teamId) {
        Team team = teamValidator.validateExistTeamAndReturnWithLeaderMember(teamId); // 1

        List<TeamMemberResponse> members = teamMemberRepository.findAllByTeamIdWithMember(teamId) // 2
                .stream()
                .map(TeamMemberResponse::of)
                .toList();

        return TeamDetailResponse.of(team, members);
    }

    @Override
    public TeamLeaderTransferResponse transferTeamLeader(Long teamId, Long loginMemberId, Long newLeaderMemberId) {
        Team team = teamValidator.validateExistTeamAndReturn(teamId);
        Member oldLeaderMember = memberValidator.validateExistMemberAndReturn(loginMemberId);
        Member newLeaderMember = memberValidator.validateExistMemberAndReturn(newLeaderMemberId);

        // 자기 자신에게 위임하려고 하는거는 아닌지
        teamValidator.validateCheckSelfAppoint(oldLeaderMember.getId(), newLeaderMember.getId());

        // 팀에 속한게 맞는지
        TeamMember oldLeaderMemberTeamMember = teamMemberValidator.validateExistTeamMemberAndReturn(oldLeaderMember.getId());
        TeamMember newLeaderMemberTeamMember = teamMemberValidator.validateExistTeamMemberAndReturn(newLeaderMember.getId());

        // 해당팀의 리더가 맞는지, 해당팀의 유저가 맞는지
        teamValidator.validateCheckTeamLeader(team, oldLeaderMember.getId());
        teamMemberValidator.validateCheckTeamMember(newLeaderMemberTeamMember);

        // 롤 변경
        oldLeaderMemberTeamMember.changeToMember();
        newLeaderMemberTeamMember.changeToLeader();
        team.transferTeamLeader(newLeaderMember);


        return TeamLeaderTransferResponse.of(team, oldLeaderMember, newLeaderMember);
    }

    @Override
    public void disbandTeam(Long teamId, Long loginMemberId) {
        Team team = teamValidator.validateExistTeamAndReturn(teamId);
        Member leaderMember = memberValidator.validateExistMemberAndReturn(loginMemberId);

        // 해당팀 팀장맞음 ?
        teamValidator.validateCheckTeamLeader(team, leaderMember.getId());

        // 인원이 팀장본인뿐인 1명인거 맞음?
        teamValidator.validateCanDisbandTeam(team.getId());

        // 이미 MATCHED 상태인 매치를 가지고있는거아님?
        teamValidator.validateCanDisBandMatchedTeam(team.getId());


        // teamMember, teamJoinRequest, teamMatch 먼저삭제 ( 하위요소니까. )
        teamMemberRepository.deleteAllByTeamId(team.getId());
        teamJoinRequestRepository.deleteAllByTeamId(team.getId());
        teamMatchRepository.deleteAllByHomeTeamId(team.getId());


        // 해당 팀 삭제
        teamRepository.deleteById(team.getId());
    }

    @Override
    public TeamNameChangeResponse changeTeamName(Long teamId, Long loginMemberId, TeamNameChangeRequest request) {
        Team team = teamValidator.validateExistTeamAndReturn(teamId);
        Member leaderMember = memberValidator.validateExistMemberAndReturn(loginMemberId);

        String previousTeamName = team.getTeamName();
        String normalizedTeamName = request.getNewTeamName().strip();

        // 해당팀에 속한거 맞음?
        TeamMember teamMember = teamMemberValidator.validateExistTeamMemberAndReturn(leaderMember.getId());
        teamMemberValidator.validateBelongsToTeam(team.getId(), teamMember);

        teamValidator.validateCheckTeamLeader(team, leaderMember.getId());


        teamValidator.validateTeamNameLength(normalizedTeamName);
        teamValidator.validateSameTeamName(team.getTeamName(), normalizedTeamName);
        teamValidator.validateExistsTeamName(normalizedTeamName);

        team.changeTeamName(normalizedTeamName);

        return TeamNameChangeResponse.of(team, previousTeamName);

    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamRankingResponse> getTeamRankings() {
        List<Team> teams = teamRepository.findAllForRanking();

        List<TeamRankingResponse> responses = new ArrayList<>(teams.size()); // 팀수 ...

        Integer previousRating = null;
        int currentRank = 0;

        for (int index = 0; index < teams.size(); index++) {
            Team team = teams.get(index); // 1530

            if (previousRating == null || previousRating != team.getRating()) {
                currentRank = index + 1;
            }

            responses.add(TeamRankingResponse.of(currentRank, team));

            previousRating = team.getRating(); // 1500
        }

        return responses;
    }


}
