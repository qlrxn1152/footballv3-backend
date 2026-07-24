package io.github.qlrxn1152.footballv3.teamjoinrequest.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.AlreadyRequestedTeamJoinException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamJoinRequestServiceImplTest {

    @Autowired private MemberService memberService;
    @Autowired private TeamService teamService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;

    @Autowired private MemberRepository memberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;

    @Test
    @DisplayName(value = "팀 가입요청 성공")
    void createTeamJoinRequest() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());

        // when
        TeamJoinRequestResponse response = teamJoinRequestService.createJoinRequest(team.getTeamId(), memberB.getMemberId());// memberB-> teamA 에 가입신청

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getMemberId()).isEqualTo(memberB.getMemberId());
        assertThat(teamMemberRepository.existsByMemberId(memberB.getMemberId())).isEqualTo(false);
        assertThat(teamMemberRepository.findAllByTeamId(team.getTeamId())).hasSize(1);
        assertThat(teamMemberRepository.findByMemberId(memberB.getMemberId())).isEmpty();
    }

    @Test
    @DisplayName(value = "존재하지 않는 팀 가입요청은 실패")
    void createTeamJoinRequest_fail_notFoundTeam() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.createJoinRequest(999L, member.getMemberId()))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");
    }

    @Test
    @DisplayName(value = "존재하지 않는 멤버 가입요청은 실패")
    void createTeamJoinRequest_fail_notFoundMember() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.createJoinRequest(team.getTeamId(), 999L))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("멤버 조회 실패");
    }

    @Test
    @DisplayName(value = "이미 팀에 속한 회원은 가입신청에 실패해야한다")
    void createTeamJoinRequest_fail_alreadyJoinedTeam() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), memberB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.createJoinRequest(team.getTeamId(), memberB.getMemberId()))
                .isInstanceOf(AlreadyJoinedTeamException.class)
                .hasMessage("이미 팀에 속한 회원입니다.");
    }

    @Test
    @DisplayName(value = "팀 가입신청은 2개이상 존재할 수 없다")
    void createTeamJoinRequest_fail_alreadyExistJoinRequest() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());
        teamJoinRequestService.createJoinRequest(team.getTeamId(), memberB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.createJoinRequest(team.getTeamId(), memberB.getMemberId()))
                .isInstanceOf(AlreadyRequestedTeamJoinException.class)
                .hasMessage("이미 팀 가입신청이 존재합니다.");
    }

    @Test
    @DisplayName(value = "팀 가입신청은 2개이상 존재할 수 없다 - 여러팀에 신청")
    void createTeamJoinRequest_fail_alreadyExistJoinRequest2() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        MemberCreateResponse memberC = memberService.signup(new MemberCreateRequest("userC", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), memberB.getMemberId());

        teamJoinRequestService.createJoinRequest(team.getTeamId(), memberC.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.createJoinRequest(teamB.getTeamId(), memberC.getMemberId()))
                .isInstanceOf(AlreadyRequestedTeamJoinException.class)
                .hasMessage("이미 팀 가입신청이 존재합니다.");
    }

}