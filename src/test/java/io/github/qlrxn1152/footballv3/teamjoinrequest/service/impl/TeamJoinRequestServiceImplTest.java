package io.github.qlrxn1152.footballv3.teamjoinrequest.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamDetailResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamMemberResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestApproveResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestListResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestMemberResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.AlreadyRequestedTeamJoinException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotFoundTeamJoinRequestException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamJoinRequestServiceImplTest {

    @Autowired private MemberService memberService;
    @Autowired private TeamService teamService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;

    @Autowired private EntityManager em;
    @Autowired private EntityManagerFactory emf;

    @Autowired private MemberRepository memberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private TeamJoinRequestRepository teamJoinRequestRepository;

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
        assertThat(teamMemberRepository.findAllByTeamIdWithMember(team.getTeamId())).hasSize(1);
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

    @Test
    @DisplayName(value = "팀 가입신청 목록 조회")
    void getTeamJoinRequests() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        MemberCreateResponse memberC = memberService.signup(new MemberCreateRequest("userC", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());
        teamJoinRequestService.createJoinRequest(team.getTeamId(), memberB.getMemberId());
        teamJoinRequestService.createJoinRequest(team.getTeamId(), memberC.getMemberId());

        // when
        TeamJoinRequestListResponse response = teamJoinRequestService.getJoinRequests(team.getTeamId(), member.getMemberId());

        // then
        assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getRequestCount()).isEqualTo(2);
        assertThat(response.getRequests()).extracting(TeamJoinRequestMemberResponse::getMemberUsername).containsExactly("userb", "userc");
    }

    @Test
    @DisplayName(value = "팀 가입신청 없는 상태에서 목록조회")
    void getTeamJoinRequests_empty() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());

        // when
        TeamJoinRequestListResponse response = teamJoinRequestService.getJoinRequests(team.getTeamId(), member.getMemberId());

        // then
        assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getRequestCount()).isZero();
        assertThat(response.getRequests()).isEmpty();
    }

    @Test
    @DisplayName(value = "팀 가입신청 없는 상태에서 목록조회 쿼리 수 확인")
    void getTeamJoinRequests_empty_checkQueryCount() throws Exception {
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());

        em.flush();
        em.clear();

        SessionFactory sessionFactory =
                emf.unwrap(SessionFactory.class);

        Statistics statistics =
                sessionFactory.getStatistics();

        statistics.clear();

        teamJoinRequestService.getJoinRequests(
                team.getTeamId(),
                member.getMemberId()
        );

        long queryCount =
                statistics.getPrepareStatementCount();

        assertThat(queryCount).isEqualTo(2);
    }


    @Test
    @DisplayName(value = "팀장이 아닌 회원은 조회에 실패해야한다")
    void getTeamJoinRequests_notTeamLeader() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), member.getMemberId());


        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.getJoinRequests(team.getTeamId(), memberB.getMemberId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");
    }

    @Test
    @DisplayName(value = "존재하지 않는 팀에는 조회에 실패해야한다")
    void getTeamJoinRequests_notFoundTeam() throws Exception {
        // given
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userA", "1234"));


        // when && then
        assertThatThrownBy(() -> teamJoinRequestService.getJoinRequests(999L, member.getMemberId()))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");
    }

    @Test
    @DisplayName(value = "팀장은 가입신청 승인할 수 있다.")
    void approveJoinRequest() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        MemberCreateResponse user = memberService.signup(new MemberCreateRequest("user", "1234"));

        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), user.getMemberId());

        // when
        TeamJoinRequestApproveResponse response = teamJoinRequestService.approveJoinRequest(team.getTeamId(), leader.getMemberId(), joinRequest.getRequestId());
        TeamDetailResponse teamDetail = teamService.getTeam(team.getTeamId());
        TeamMember approvedMember = teamMemberRepository.findByMemberId(user.getMemberId()).get();

        // then
        assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getTeamRole()).isEqualTo(TeamRole.MEMBER);
        assertThat(response.getMemberId()).isEqualTo(user.getMemberId());
        assertThat(teamDetail.getMemberCount()).isEqualTo(2);
        assertThat(teamDetail.getMembers()).extracting(TeamMemberResponse::getUsername).containsExactly("leadermember", "user");
        assertThat(approvedMember.getMember().getId()).isEqualTo(user.getMemberId());
        assertThat(approvedMember.getTeam().getId()).isEqualTo(team.getTeamId());
        assertThat(approvedMember.getRole()).isEqualTo(TeamRole.MEMBER);
        assertThat(teamJoinRequestRepository.findById(joinRequest.getRequestId()).isEmpty()).isTrue();
    }

    @Test
    @DisplayName(value = "팀장은 아닌 회원은 가입신청 수락에 실패해야한다.")
    void approveJoinRequest_fail_notTeamLeader() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        MemberCreateResponse user = memberService.signup(new MemberCreateRequest("user", "1234"));

        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), user.getMemberId());

        // when
        assertThatThrownBy(() -> teamJoinRequestService.approveJoinRequest(team.getTeamId(), user.getMemberId(), joinRequest.getRequestId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");
    }

    @Test
    @DisplayName(value = "존재하지 않는 가입신청은 승인에 실패해야한다.")
    void approveJoinRequest_fail_notFoundRequest() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when
        assertThatThrownBy(() -> teamJoinRequestService.approveJoinRequest(team.getTeamId(), leader.getMemberId(), 999L))
                .isInstanceOf(NotFoundTeamJoinRequestException.class)
                .hasMessage("가입신청 조회 실패");
    }

    @Test
    @DisplayName(value = "자신의 팀이 아닌 다른팀의 요청은 승인할 수 없다.")
    void approveJoinRequest_fail_notSameTeamLeader() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse user = memberService.signup(new MemberCreateRequest("user", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), user.getMemberId());

        // when
        assertThatThrownBy(() -> teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderB.getMemberId(), joinRequest.getRequestId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");
    }





}