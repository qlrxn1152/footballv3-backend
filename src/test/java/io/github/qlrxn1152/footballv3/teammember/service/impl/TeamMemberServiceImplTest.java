package io.github.qlrxn1152.footballv3.teammember.service.impl;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamDetailResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotTeamMemberException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import io.github.qlrxn1152.footballv3.teammember.service.TeamMemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamMemberServiceImplTest {

    @Autowired private MemberService memberService;
    @Autowired private TeamService teamService;
    @Autowired private TeamMemberService teamMemberService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;

    @Autowired private TeamMemberRepository teamMemberRepository;

    @Test
    @DisplayName(value = "일반 팀원은 자신의 팀에서 탈퇴할 수 있다.")
    void leaveTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());

        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());

        // when
        teamMemberService.leaveTeam(team.getTeamId(), normalMember.getMemberId());

        // then
        assertThat(teamMemberRepository.existsByMemberId(normalMember.getMemberId())).isFalse();
    }

    @Test
    @DisplayName(value = "일반 팀원이 탈퇴하면, 팀 인원이 감소한다.")
    void leaveTeam_decreaseMemberCount() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());

        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());

        // when
        TeamDetailResponse teamDetailResponseBefore = teamService.getTeam(team.getTeamId()); // 2
        teamMemberService.leaveTeam(team.getTeamId(), normalMember.getMemberId());
        TeamDetailResponse teamDetailResponseAfter = teamService.getTeam(team.getTeamId()); // 1

        // then
        assertThat(teamMemberRepository.existsByMemberId(normalMember.getMemberId())).isFalse();
        assertThat( (teamDetailResponseBefore.getMemberCount() - teamDetailResponseAfter.getMemberCount()) ).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "팀장은 팀 탈퇴에 실패해야한다.")
    void leaveTeam_fail_leader() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());


        // when && then
        assertThatThrownBy(() -> teamMemberService.leaveTeam(team.getTeamId(), leaderMember.getMemberId()))
                .isInstanceOf(NotTeamMemberException.class)
                .hasMessage("해당팀의 일반 유저가 아닙니다.");
    }

    @Test
    @DisplayName(value = "팀에 소속되지 않은 회원은 탈퇴할 수 없다.")
    void leaveTeam_fail_notFoundTeamMember() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());


        // when && then
        assertThatThrownBy(() -> teamMemberService.leaveTeam(team.getTeamId(), normalMember.getMemberId()))
                .isInstanceOf(NotJoinedTeamException.class)
                .hasMessage("팀에 속한 회원이 아닙니다.");
    }

    @Test
    @DisplayName(value = "자신이 소속되지 않은 팀 주소로 탈퇴할 수 없다.")
    void leaveTeam_fail_notBelongToTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse leaderMemberB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderMemberB.getMemberId());

        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());


        // when && then
        assertThatThrownBy(() -> teamMemberService.leaveTeam(teamB.getTeamId(), normalMember.getMemberId()))
                .isInstanceOf(NotSameTeamException.class)
                .hasMessage("해당팀이 아닙니다.");
    }

    @Test
    @DisplayName(value = "존재하지 않는 팀")
    void leaveTeam_fail_notFoundTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());

        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());


        // when && then
        assertThatThrownBy(() -> teamMemberService.leaveTeam(999L, normalMember.getMemberId()))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");
    }

    @Test
    @DisplayName(value = "멤버 조회 실패")
    void leaveTeam_fail_notFoundMember() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());

        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());


        // when && then
        assertThatThrownBy(() -> teamMemberService.leaveTeam(team.getTeamId(), 1982L))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("멤버 조회 실패");
    }

    @Test
    @DisplayName(value = "일반회원이 팀 탈퇴이후, 팀에 가입신청을 넣을 수 있다.")
    void leaveTeam_canRequestAnotherTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());

        MemberCreateResponse normalMember = memberService.signup(new MemberCreateRequest("normalMember", "1234"));
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());

        teamMemberService.leaveTeam(team.getTeamId(), normalMember.getMemberId()); // 팀 탈퇴

        // when
        TeamJoinRequestResponse reJoinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), normalMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), reJoinRequest.getRequestId());

        // then
        assertThat(teamMemberRepository.existsByMemberId(normalMember.getMemberId())).isTrue();

    }




}