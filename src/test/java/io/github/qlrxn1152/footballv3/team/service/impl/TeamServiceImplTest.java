package io.github.qlrxn1152.footballv3.team.service.impl;

import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamListResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.DuplicateTeamNameException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.TeamNameLengthException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
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

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;

    @Autowired private EntityManagerFactory emf;
    @Autowired private EntityManager em;

    @BeforeEach
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
    @DisplayName(value = "팀 전체조회_쿼리튜닝 이전")
    void findTeams_before() throws Exception {

        em.flush();
        em.clear();

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        statistics.clear();

        teamService.getTeams(); // 실제 실행코드 => 즉, 유저가 팀들 조회를 하고싶어서 /api/teams GET 요청을 보냄

        long queryCount = statistics.getPrepareStatementCount();

        System.out.println("쿼리 실행 갯수 : " + queryCount);
    }




}