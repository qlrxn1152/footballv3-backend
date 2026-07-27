package io.github.qlrxn1152.footballv3.team.service.impl;

import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamDetailResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamLeaderTransferResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamListResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.*;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.repository.TeamJoinRequestRepository;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotTeamMemberException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import io.github.qlrxn1152.footballv3.teammember.service.TeamMemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamServiceImplTest {

    @Autowired private TeamService teamService;
    @Autowired private MemberService memberService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;
    @Autowired private TeamMemberService teamMemberService;

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private MemberRepository  memberRepository;
    @Autowired private TeamJoinRequestRepository teamJoinRequestRepository;

    @Autowired private EntityManagerFactory emf;
    @Autowired private EntityManager em;

//    @BeforeEach
    void setUp() {
        for (int i = 1; i <= 100; i++) {
            MemberCreateResponse member =
                    memberService.signup(
                            new MemberCreateRequest(
                                    "user" + i,
                                    "1234"
                            )
                    );

            teamService.createTeam(
                    new TeamCreateRequest(
                            "team" + i
                    ),
                    member.getMemberId()
            );
        }
    }

    @Test
    @DisplayName(value = "팀 생성")
    void createTeam() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when
        TeamCreateResponse teamResponse = teamService.createTeam(new TeamCreateRequest("teamA"), memberResponse.getMemberId());
        Team savedTeam = teamRepository.findById(teamResponse.getTeamId()).get();
        TeamMember savedTeamMember = teamMemberRepository.findByMemberId(memberResponse.getMemberId()).get();

        // then
        assertThat(teamResponse).isNotNull();
        assertThat(teamResponse.getTeamId()).isNotNull();
        assertThat(teamResponse.getTeamName()).isNotNull();

        assertThat(savedTeam.getId()).isEqualTo(teamResponse.getTeamId());
        assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(memberResponse.getMemberId());
        assertThat(teamMemberRepository.existsByMemberId(memberResponse.getMemberId())).isTrue();
        assertThat(savedTeamMember.getRole()).isEqualTo(TeamRole.LEADER);
        assertThat(savedTeamMember.getTeam().getId()).isEqualTo(savedTeam.getId());
    }

    @Test
    @DisplayName(value = "팀생성_팀 이름 공백제거 확인")
    void createTeam_strip() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when
        TeamCreateResponse teamResponse = teamService.createTeam(new TeamCreateRequest("   teamA   "), memberResponse.getMemberId());
        Team savedTeam = teamRepository.findById(teamResponse.getTeamId()).get();
        TeamMember savedTeamMember = teamMemberRepository.findByMemberId(memberResponse.getMemberId()).get();

        // then
        assertThat(teamResponse).isNotNull();
        assertThat(teamResponse.getTeamId()).isNotNull();
        assertThat(teamResponse.getTeamName()).isNotNull();

        assertThat(savedTeam.getId()).isEqualTo(teamResponse.getTeamId());
        assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(memberResponse.getMemberId());
        assertThat(savedTeam.getTeamName()).isEqualTo("teamA");
        assertThat(teamMemberRepository.existsByMemberId(memberResponse.getMemberId())).isTrue();
        assertThat(savedTeamMember.getRole()).isEqualTo(TeamRole.LEADER);
        assertThat(savedTeamMember.getTeam().getId()).isEqualTo(savedTeam.getId());
    }

    @Test
    @DisplayName(value = "팀 생성 실패_이미 팀에 가입")
    void createTeam_fail_alreadyJoinTeam() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));
        teamService.createTeam(new TeamCreateRequest("teamA"), memberResponse.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("teamB"), memberResponse.getMemberId()))
                .isInstanceOf(AlreadyJoinedTeamException.class)
                .hasMessage("이미 팀에 속한 회원입니다.");
    }

    @Test
    @DisplayName(value = "팀 생성 실패_팀 이름 이미존재")
    void createTeam_fail_duplicateTeamName() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberResponseB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        teamService.createTeam(new TeamCreateRequest("teamA"), memberResponse.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("teamA"), memberResponseB.getMemberId()))
                .isInstanceOf(DuplicateTeamNameException.class)
                .hasMessage("팀 이름 중복");
    }

    @Test
    @DisplayName(value = "팀 생성 실패_팀 이름 길이 미충족(부족)")
    void createTeam_fail_teamNameTooShort() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("a"), memberResponse.getMemberId()))
                .isInstanceOf(TeamNameLengthException.class)
                .hasMessage("팀 이름은 2~10글자까지만 가능합니다.");
    }

    @Test
    @DisplayName(value = "팀 생성 실패_팀 이름 길이 미충족(넘음)")
    void createTeam_fail_teamNameTooLong() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("aasdfasgdasdrfasdg"), memberResponse.getMemberId()))
                .isInstanceOf(TeamNameLengthException.class)
                .hasMessage("팀 이름은 2~10글자까지만 가능합니다.");
    }

    @Test
    @DisplayName(value = "팀 전체조회")
    void findTeams() throws Exception {
        // given
        MemberCreateResponse memberA = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        teamService.createTeam(new TeamCreateRequest("teamA"), memberA.getMemberId());
        teamService.createTeam(new TeamCreateRequest("teamB"), memberB.getMemberId());

        // when
        List<TeamListResponse> response = teamService.getTeams();

        // then
        assertThat(response).hasSize(2);
        assertThat(response).extracting(TeamListResponse::getTeamName).containsExactly("teamB", "teamA");
    }

    @Test
    @DisplayName(value = "팀 전체조회_팀 존재 안함")
    void findTeams_empty() throws Exception {
        // when
        List<TeamListResponse> response = teamService.getTeams();

        // then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName(value = "팀 상세 조회")
    void getTeam() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("userA", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when
        TeamDetailResponse response = teamService.getTeam(team.getTeamId());

        // then
        assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getTeamName()).isEqualTo("teamA");
        assertThat(response.getLeaderMemberId()).isEqualTo(leader.getMemberId());
        assertThat(response.getMemberCount()).isEqualTo(1);
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getTeamRole()).isEqualTo(TeamRole.LEADER);
    }

    @Test
    @DisplayName(value = "팀 상세 조회_멤버많음")
    void getTeam2() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse member = memberService.signup(new MemberCreateRequest("userB", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when

        Team savedTeam = teamRepository.findById(team.getTeamId()).get();
        Member savedMember = memberRepository.findById(member.getMemberId()).get();

        teamMemberRepository.save(TeamMember.joinTeam(savedTeam, savedMember));

        TeamDetailResponse response = teamService.getTeam(team.getTeamId());

        // then
        assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getTeamName()).isEqualTo("teamA");
        assertThat(response.getLeaderMemberId()).isEqualTo(leader.getMemberId());
        assertThat(response.getMemberCount()).isEqualTo(2);
        assertThat(response.getMembers()).hasSize(2);
    }

    @Test
    @DisplayName(value = "팀 상세 조회실패 _ 팀존재안함")
    void getTeam_fail_notFoundTeam() throws Exception {
        // when && then

        assertThatThrownBy(() -> teamService.getTeam(999L))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");
    }


    @Test
    @DisplayName(value = "팀 상세 조회 쿼리 수 체크")
    void getTeam_check_query_count() throws Exception {
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("userA", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        em.flush();
        em.clear();

        SessionFactory sessionFactory =
                emf.unwrap(
                        SessionFactory.class
                );

        Statistics statistics =
                sessionFactory.getStatistics();

        statistics.clear();

        teamService.getTeam(team.getTeamId());

        long queryCount =
                statistics.getPrepareStatementCount();

        System.out.println(
                "팀 상세 조회 쿼리 개수 : " + queryCount
        );

        assertThat(queryCount).isEqualTo(2);
    }

    @Test
    @DisplayName(value = "팀장 변경")
    void transferTeamLeader() throws Exception {
        // given
        MemberCreateResponse oldLeaderMember = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeaderMember = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeaderMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeaderMember.getMemberId(), joinRequest.getRequestId());

        // when
        teamService.transferTeamLeader(team.getTeamId(), oldLeaderMember.getMemberId(), newLeaderMember.getMemberId());
        TeamMember oldTeamMember = teamMemberRepository.findByMemberId(oldLeaderMember.getMemberId()).get();
        TeamMember newTeamMember = teamMemberRepository.findByMemberId(newLeaderMember.getMemberId()).get();

        Team afterTeam = teamRepository.findById(team.getTeamId()).get();

        // then
        assertThat(afterTeam.getLeaderMember().getId()).isEqualTo(newLeaderMember.getMemberId());
        assertThat(oldTeamMember.getRole()).isEqualTo(TeamRole.MEMBER);
        assertThat(newTeamMember.getRole()).isEqualTo(TeamRole.LEADER);
    }

    @Test
    @DisplayName(value = "팀장이 아닌 회원은 변경에 실패해야한다.")
    void transferTeamLeader_fail_notTeamLeader() throws Exception {
        // given
        MemberCreateResponse oldLeaderMember = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeaderMember = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeaderMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeaderMember.getMemberId(), joinRequest.getRequestId());

        // when && then
        assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), newLeaderMember.getMemberId(), newLeaderMember.getMemberId()))
                .isInstanceOf(SameTeamLeaderException.class)
                .hasMessage("자기 자신에게 팀장 위임은 불가합니다.");
    }

    @Test
    @DisplayName(value = "자기 자신에게 변경을 시도할 수 없다.")
    void transferTeamLeader_fail_self() throws Exception {
        // given
        MemberCreateResponse oldLeaderMember = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeaderMember = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeaderMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeaderMember.getMemberId(), joinRequest.getRequestId());

        // when && then
        assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), oldLeaderMember.getMemberId(), oldLeaderMember.getMemberId()))
                .isInstanceOf(SameTeamLeaderException.class)
                .hasMessage("자기 자신에게 팀장 위임은 불가합니다.");
    }

    @Test
    @DisplayName(value = "팀에 소속되지 않은 회원에게는 위임에 실패한다.")
    void transferTeamLeader_fail_notJoinTeamMember() throws Exception {
        // given
        MemberCreateResponse oldLeaderMember = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeaderMember = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeaderMember.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), oldLeaderMember.getMemberId(), newLeaderMember.getMemberId()))
                .isInstanceOf(NotJoinedTeamException.class)
                .hasMessage("팀에 속한 회원이 아닙니다.");
    }

    @Test
    @DisplayName(value = "해당팀에 소속되지 않은 회원에게는 위임에 실패한다.")
    void transferTeamLeader_fail_notJoinSameTeamMember() throws Exception {
        // given
        MemberCreateResponse oldLeaderMember = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeaderMember = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeaderMember.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), newLeaderMember.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), oldLeaderMember.getMemberId(), newLeaderMember.getMemberId()))
                .isInstanceOf(NotTeamMemberException.class)
                .hasMessage("해당팀의 일반 유저가 아닙니다.");
    }

    @Test
    @DisplayName(value = "새로운 팀장은 팀장 전용 기능을 사용할 수 있다.")
    void transferTeamLeader_canTeamLeaderMethods() throws Exception {
        // given
        MemberCreateResponse oldLeaderMember = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeaderMember = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeaderMember.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeaderMember.getMemberId(), joinRequest.getRequestId());

        // when
        teamService.transferTeamLeader(team.getTeamId(), oldLeaderMember.getMemberId(), newLeaderMember.getMemberId()); // 팀장변경

        // then
        assertThatCode(() -> teamJoinRequestService.getJoinRequests(team.getTeamId(), newLeaderMember.getMemberId())).doesNotThrowAnyException();
        assertThatCode(() -> teamMemberService.leaveTeam(team.getTeamId(), oldLeaderMember.getMemberId())).doesNotThrowAnyException();
        assertThatThrownBy(() -> teamJoinRequestService.getJoinRequests(team.getTeamId(), oldLeaderMember.getMemberId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");
    }

    @Test
    @DisplayName(value = "팀 인원이 1명뿐인 팀장은 해당팀을 해체할 수 있다.")
    void disbandTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse memberA = memberService.signup(new MemberCreateRequest("memberA", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());
        teamJoinRequestService.createJoinRequest(team.getTeamId(), memberA.getMemberId());

        // when
        teamService.disbandTeam(team.getTeamId(), leaderMember.getMemberId());

        // then
        assertThat(teamMemberRepository.existsByMemberId(leaderMember.getMemberId())).isFalse();
        assertThat(teamJoinRequestRepository.findAllByTeamId(team.getTeamId())).isEmpty();
        assertThat(memberRepository.findById(leaderMember.getMemberId()).get().getId()).isEqualTo(leaderMember.getMemberId());
        assertThat(teamMemberRepository.findAllByTeamIdWithMember(team.getTeamId())).isEmpty();
    }

    @Test
    @DisplayName(value = "팀장이 아닌 회원은 팀을 해체할 수 없다.")
    void disbandTeam_fail_notTeamLeader() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse memberA = memberService.signup(new MemberCreateRequest("memberA", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), memberA.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());

        // when && then
        assertThat(teamMemberRepository.existsByMemberId(leaderMember.getMemberId())).isTrue();
        assertThat(teamMemberRepository.findAllByTeamIdWithMember(team.getTeamId())).isNotEmpty();

        assertThatThrownBy(() -> teamService.disbandTeam(team.getTeamId(), memberA.getMemberId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");
    }

    @Test
    @DisplayName(value = "존재하지 않는 팀은 해체할 수 없다.")
    void disbandTeam_fail_notFoundTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse memberA = memberService.signup(new MemberCreateRequest("memberA", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), memberA.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());

        // when && then
        assertThatThrownBy(() -> teamService.disbandTeam(999L, leaderMember.getMemberId()))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");
    }

    @Test
    @DisplayName(value = "존재하지 않는 회원은 해체할 수 없다.")
    void disbandTeam_fail_notFoundMember() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse memberA = memberService.signup(new MemberCreateRequest("memberA", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), memberA.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leaderMember.getMemberId(), joinRequest.getRequestId());

        // when && then
        assertThatThrownBy(() -> teamService.disbandTeam(team.getTeamId(), 999L))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("멤버 조회 실패");
    }

    @Test
    @DisplayName(value = "한개의 팀을 삭제한다고해서, 다른팀에는 영향을 미치지않는다.")
    void disbandTeam_keepOtherTeam() throws Exception {
        // given
        MemberCreateResponse leaderMember = memberService.signup(new MemberCreateRequest("leaderMember", "1234"));
        MemberCreateResponse memberA = memberService.signup(new MemberCreateRequest("memberA", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leaderMember.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), memberA.getMemberId());

        // when
        teamService.disbandTeam(team.getTeamId(), leaderMember.getMemberId()); // team -> 삭제

        // then
        assertThat(teamMemberRepository.existsByMemberId(leaderMember.getMemberId())).isFalse();
        assertThat(memberRepository.findById(leaderMember.getMemberId()).get().getId()).isEqualTo(leaderMember.getMemberId());
        assertThat(teamMemberRepository.findAllByTeamIdWithMember(team.getTeamId())).isEmpty();

        assertThat(teamMemberRepository.existsByMemberId(memberA.getMemberId())).isTrue();
        assertThat(memberRepository.findById(memberA.getMemberId()).get().getId()).isEqualTo(memberA.getMemberId());
        assertThat(teamMemberRepository.findAllByTeamIdWithMember(teamB.getTeamId())).isNotEmpty();
    }

    @Test
    @DisplayName(value = "팀장을 위임 한 이후에는, 새로운 팀장만 팀 해체를 할 수 있다.")
    void disbandTeam_fail_afterTransferLeader() throws Exception {
        // given
        MemberCreateResponse oldLeader = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeader = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeader.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeader.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeader.getMemberId(), joinRequest.getRequestId());
        teamService.transferTeamLeader(team.getTeamId(), oldLeader.getMemberId(), newLeader.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.disbandTeam(team.getTeamId(), oldLeader.getMemberId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");

        teamMemberService.leaveTeam(team.getTeamId(), oldLeader.getMemberId());

        assertThatCode(() -> teamService.disbandTeam(team.getTeamId(), newLeader.getMemberId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName(value = "팀 해체 이후에는, 다시 팀을 만들 수 있다.")
    void disbandTeam_fail_afterTransferLeader_newCreateTeam() throws Exception {
        // given
        MemberCreateResponse oldLeader = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeader = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeader.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeader.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeader.getMemberId(), joinRequest.getRequestId());
        teamService.transferTeamLeader(team.getTeamId(), oldLeader.getMemberId(), newLeader.getMemberId());
        teamMemberService.leaveTeam(team.getTeamId(), oldLeader.getMemberId());
        teamService.disbandTeam(team.getTeamId(), newLeader.getMemberId());

        // when && then
        assertThatCode(() -> teamService.createTeam(new TeamCreateRequest("teamB"), newLeader.getMemberId())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName(value = "팀 해체 이후에는, 새로 만든팀에 가입신청을 넣을 수 있어야한다.")
    void disbandTeam_fail_afterTransferLeader_newCreateTeam_joinRequest() throws Exception {
        // given
        MemberCreateResponse oldLeader = memberService.signup(new MemberCreateRequest("oldLeader", "1234"));
        MemberCreateResponse newLeader = memberService.signup(new MemberCreateRequest("newLeader", "1234"));
        MemberCreateResponse memberB = memberService.signup(new MemberCreateRequest("memberB", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), oldLeader.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeader.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), oldLeader.getMemberId(), joinRequest.getRequestId());
        teamService.transferTeamLeader(team.getTeamId(), oldLeader.getMemberId(), newLeader.getMemberId());
        teamMemberService.leaveTeam(team.getTeamId(), oldLeader.getMemberId());
        teamService.disbandTeam(team.getTeamId(), newLeader.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), newLeader.getMemberId());

        // when && then
        assertThatCode(() -> teamJoinRequestService.createJoinRequest(teamB.getTeamId(), memberB.getMemberId())).doesNotThrowAnyException();
        assertThat(teamJoinRequestRepository.findByTeamIdAndMemberId(teamB.getTeamId(), memberB.getMemberId())).isNotEmpty();
    }


}