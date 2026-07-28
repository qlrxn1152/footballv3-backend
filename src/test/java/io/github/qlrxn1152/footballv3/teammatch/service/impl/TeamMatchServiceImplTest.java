package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchPendingResponse;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.DuplicateMatchRegistrationException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.InvalidMatchPlatedAtException;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class TeamMatchServiceImplTest {

    @Autowired private TeamMatchService teamMatchService;
    @Autowired private TeamService teamService;
    @Autowired private MemberService memberService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;

    @Autowired private TeamMatchRepository teamMatchRepository;

    @Autowired private EntityManager em;
    @Autowired private EntityManagerFactory emf;


    private LocalDateTime teamMatchPlayedAt = LocalDateTime.of(3000, 1, 1, 1, 1);
    private LocalDateTime invalidTeamMatchPlayedAt = LocalDateTime.of(2026, 1, 1, 1, 1);

    @Test
    @DisplayName(value = "홈팀 팀장은 등록한 매치가 없다면, 매치를 등록할 수 있다.")
    void registerTeamMatch() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when
        TeamMatchCreateResponse response = teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatch savedTeamMatchEntity = teamMatchRepository.findById(response.getMatchId()).get();

        // then
        assertThat(response.getMatchId()).isEqualTo(savedTeamMatchEntity.getId());
        assertThat(response.getHomeTeamId()).isEqualTo(team.getTeamId());
        assertThat(response.getStatus()).isEqualTo(TeamMatchStatus.PENDING);
        assertThat(response.getPlayedAt()).isEqualTo(teamMatchPlayedAt);
        assertThat(response.getPlayedAt().isAfter(response.getCreatedAt())).isTrue();

        assertThat(savedTeamMatchEntity.getHomeTeam().getId()).isEqualTo(team.getTeamId());
        assertThat(savedTeamMatchEntity.getHomeTeam().getId()).isEqualTo(response.getHomeTeamId());
        assertThat(savedTeamMatchEntity.getStatus()).isEqualTo(TeamMatchStatus.PENDING);
        assertThat(savedTeamMatchEntity.getAwayTeam()).isNull();
        assertThat(savedTeamMatchEntity.getPlayedAt().isAfter(response.getCreatedAt())).isTrue();
    }

    @Test
    @DisplayName(value = "팀장이 아닌 회원은 매치 등록에 실패한다.")
    void registerTeamMatch_fail_notTeamLeader() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        MemberCreateResponse user = memberService.signup(new MemberCreateRequest("user", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), user.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leader.getMemberId(), joinRequest.getRequestId());

        // when && then
        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), user.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt)))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");

        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), user.getMemberId(), new TeamMatchCreateRequest(LocalDateTime.of(2026, 1,1,1,1))))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");

        assertThat(teamMatchRepository.existsByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName(value = "경기 예정 일시가 현재보다 과거일시면, 매치 등록에 실패한다.")
    void registerTeamMatch_fail_notFuture() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(invalidTeamMatchPlayedAt)))
                .isInstanceOf(InvalidMatchPlatedAtException.class)
                .hasMessage("경기 예정 일시는 현재 시간보다 미래여야 합니다.");

        assertThat(teamMatchRepository.existsByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName(value = "경기 예정 일시가 현재와 같은 시간이면, 매치 등록에 실패한다.")
    void registerTeamMatch_fail_notFuture_current() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(LocalDateTime.now())))
                .isInstanceOf(InvalidMatchPlatedAtException.class)
                .hasMessage("경기 예정 일시는 현재 시간보다 미래여야 합니다.");

        assertThat(teamMatchRepository.existsByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isFalse();
    }

    @Test
    @DisplayName(value = "PENDING 상태인 매치가 이미 존재하면, 매치등록에 실패해야한다.")
    void registerTeamMatch_fail_duplicate_registration_match() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));

        // when && then
        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1))))
                .isInstanceOf(DuplicateMatchRegistrationException.class)
                .hasMessage("이미 대기중인 매치가 존재합니다.");

        assertThat(teamMatchRepository.countByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "존재하지 않는 팀")
    void registerTeamMatch_fail_notFoundTeam() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchService.registerMatch(999L, leader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt)))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");

        assertThat(teamMatchRepository.countByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isZero();
    }

    @Test
    @DisplayName(value = "존재하지 않는 멤버")
    void registerTeamMatch_fail_notFoundMember() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), 1234L, new TeamMatchCreateRequest(teamMatchPlayedAt)))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("멤버 조회 실패");

        assertThat(teamMatchRepository.countByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isZero();
    }

    @Test
    @DisplayName(value = "팀장 위임이후에는, 이전팀장은 매치등록에 실패해야하고 새로운 팀장은 매치 등록이 가능해야한다.")
    void registerTeamMatch_fail_transferLeader() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));
        MemberCreateResponse newLeader = memberService.signup(new MemberCreateRequest("newLeader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(team.getTeamId(), newLeader.getMemberId());
        teamJoinRequestService.approveJoinRequest(team.getTeamId(), leader.getMemberId(), joinRequest.getRequestId());
        teamService.transferTeamLeader(team.getTeamId(), leader.getMemberId(), newLeader.getMemberId());

        // when && then
        assertThatCode(() -> teamMatchService.registerMatch(team.getTeamId(), newLeader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt))).doesNotThrowAnyException();
        assertThatThrownBy(() -> teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1))))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");

        assertThat(teamMatchRepository.countByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "팀 해체 이후에는, 해당팀의 매치기록이 같이 삭제되어야한다.")
    void registerTeamMatch_fail_disbandTeam() throws Exception {
        // given
        MemberCreateResponse leader = memberService.signup(new MemberCreateRequest("leader", "1234"));

        TeamCreateResponse team = teamService.createTeam(new TeamCreateRequest("teamA"), leader.getMemberId());
        teamMatchService.registerMatch(team.getTeamId(), leader.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));

        // when
        teamService.disbandTeam(team.getTeamId(), leader.getMemberId()); // 팀 해체

        // then
        assertThat(teamMatchRepository.countByHomeTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isZero();
    }

    @Test
    @DisplayName(value = "여러팀의 PENDING 매치 조회할 수 있다.")
    void getPendingMatches() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        teamMatchService.registerMatch(teamA.getTeamId(),  leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        teamMatchService.registerMatch(teamB.getTeamId(),  leaderB.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));

        // when
        List<TeamMatchPendingResponse> response = teamMatchService.getPendingMatches();

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response).extracting(TeamMatchPendingResponse::getHomeTeamName).containsExactly("teamA", "teamB");
        assertThat(response).allMatch(match -> match.getStatus() == TeamMatchStatus.PENDING);
    }

    @Test
    @DisplayName(value = "경기 시간이 가까운 순서로, 정렬된다.")
    void getPendingMatches_orderby() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        teamMatchService.registerMatch(teamA.getTeamId(),  leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(2)));
        teamMatchService.registerMatch(teamB.getTeamId(),  leaderB.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(3)));

        // when
        List<TeamMatchPendingResponse> response = teamMatchService.getPendingMatches();

        // then
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.get(0).getPlayedAt()).isEqualTo(teamMatchPlayedAt.plusDays(2));
        assertThat(response.get(1).getPlayedAt()).isEqualTo(teamMatchPlayedAt.plusDays(3));
    }

    @Test
    @DisplayName(value = "PENDING 매치가 존재하지않으면, 빈 데이터를 반환한다.")
    void getPendingMatches_empty() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());

        // when
        List<TeamMatchPendingResponse> response = teamMatchService.getPendingMatches();

        // then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName(value = "N+1 문제가 발생하지않으며, 1번의 쿼리로 PENDING 매치들을 다 가지고온다. ( homeTeam 에 대한 정보도 같이 가지고옴 )")
    void getPendingMatches_check_queryCount() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        teamMatchService.registerMatch(teamA.getTeamId(),  leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(2)));
        teamMatchService.registerMatch(teamB.getTeamId(),  leaderB.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(3)));

        em.flush();
        em.clear();

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        statistics.clear();

        // when
        teamMatchService.getPendingMatches();

        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(queryCount).isEqualTo(1);

    }







}