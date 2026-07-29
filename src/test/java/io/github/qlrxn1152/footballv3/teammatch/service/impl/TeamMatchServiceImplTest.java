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
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.*;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.*;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchResultService;
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
    @Autowired private TeamMatchResultService teamMatchResultService;

    @Autowired private TeamMatchRepository teamMatchRepository;

    @Autowired private EntityManager em;
    @Autowired private EntityManagerFactory emf;


    private LocalDateTime teamMatchPlayedAt = LocalDateTime.of(3000, 1, 1, 1, 1);
    private LocalDateTime invalidTeamMatchPlayedAt = LocalDateTime.of(2026, 1, 1, 1, 1);

    private MemberCreateResponse createMember(String username) {
        return memberService.signup(new MemberCreateRequest(username, "1234"));
    }

    private TeamCreateResponse createTeam(String teamName, Long memberId) {
        return teamService.createTeam(new TeamCreateRequest(teamName), memberId);
    }

    private TeamMatchCreateResponse registerPendingMatch(Long homeTeamId, Long loginMemberId, LocalDateTime playedAt) {
        return teamMatchService.registerMatch(homeTeamId, loginMemberId, new TeamMatchCreateRequest(playedAt));
    }

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

        assertThat(teamMatchRepository.countByTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isEqualTo(1);
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

        assertThat(teamMatchRepository.countByTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isZero();
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

        assertThat(teamMatchRepository.countByTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isZero();
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

        assertThat(teamMatchRepository.countByTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isEqualTo(1);
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
        assertThat(teamMatchRepository.countByTeamIdAndStatus(team.getTeamId(), TeamMatchStatus.PENDING)).isZero();
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

    @Test
    @DisplayName(value = "다른 팀의 팀장은 PENDING 매치를 수락할 수 있다.")
    void acceptMatch() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when
        TeamMatchAcceptResponse response = teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        TeamMatch entityTeamMatch = teamMatchRepository.findById(response.getMatchId()).get();

        // then
        assertThat(response.getMatchId()).isEqualTo(match.getMatchId());
        assertThat(entityTeamMatch.getStatus()).isEqualTo(TeamMatchStatus.MATCHED);
        assertThat(entityTeamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
        assertThat(entityTeamMatch.getHomeTeam().getId()).isEqualTo(teamA.getTeamId());
        assertThat(entityTeamMatch.getMatchedAt().isAfter(entityTeamMatch.getCreatedAt())).isTrue();
    }

    @Test
    @DisplayName(value = "MATCHED 로 변경된 매치는, PENDING 목록조회에서 제외 되어야한다.")
    void acceptMatch_removedFromPendingList() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when
        TeamMatchAcceptResponse response = teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        List<TeamMatchPendingResponse> pendingMatches = teamMatchService.getPendingMatches();
        TeamMatch entityTeamMatch = teamMatchRepository.findById(response.getMatchId()).get();

        // then
        assertThat(response.getMatchId()).isEqualTo(match.getMatchId());
        assertThat(entityTeamMatch.getStatus()).isEqualTo(TeamMatchStatus.MATCHED);
        assertThat(entityTeamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
        assertThat(entityTeamMatch.getHomeTeam().getId()).isEqualTo(teamA.getTeamId());
        assertThat(entityTeamMatch.getMatchedAt().isAfter(entityTeamMatch.getCreatedAt())).isTrue();

        assertThat(pendingMatches).isEmpty();
    }

    @Test
    @DisplayName(value = "MATCHED 로 변경된 매치를 가진 팀은, 추가로 PENDING 매치를 등록할 수 있다.")
    void acceptMatch_canRegisterNewPendingMatch() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when
        TeamMatchAcceptResponse response = teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        TeamMatchCreateResponse reMatchCreateResponse = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        TeamMatch reMatchEntity = teamMatchRepository.findById(reMatchCreateResponse.getMatchId()).get();
        List<TeamMatchPendingResponse> pendingMatches = teamMatchService.getPendingMatches();

        // then
        assertThat(pendingMatches).extracting(TeamMatchPendingResponse::getHomeTeamName).containsExactly("teamA");
        assertThat(reMatchEntity.getId()).isEqualTo(reMatchCreateResponse.getMatchId());
        assertThat(reMatchEntity.getStatus()).isEqualTo(TeamMatchStatus.PENDING);
        assertThat(reMatchEntity.getHomeTeam().getId()).isEqualTo(teamA.getTeamId());
        assertThat(reMatchEntity.getAwayTeam()).isNull();
    }

    @Test
    @DisplayName(value = "자신의 팀이 등록한 매치는 매치수락을 할 수 없다.")
    void acceptMatch_fail_sameTeam() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when && then
        assertThatThrownBy(() -> teamMatchService.acceptMatch(match.getMatchId(), teamA.getTeamId(), leaderA.getMemberId()))
                .isInstanceOf(SameTeamMatchAcceptException.class)
                .hasMessage("자신의 팀이 등록한 매치는 수락할 수 없습니다.");
    }

    @Test
    @DisplayName(value = "일반 팀원은 매치 수락을 할 수 없다.")
    void acceptMatch_fail_notTeamLeader() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse member = createMember("member");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamJoinRequestResponse joinRequest = teamJoinRequestService.createJoinRequest(teamA.getTeamId(), member.getMemberId());
        teamJoinRequestService.approveJoinRequest(teamA.getTeamId(), leaderA.getMemberId(), joinRequest.getRequestId());

        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when && then
        assertThatThrownBy(() -> teamMatchService.acceptMatch(match.getMatchId(), teamA.getTeamId(), member.getMemberId()))
                .isInstanceOf(NotTeamLeaderException.class)
                .hasMessage("팀장이 아닙니다.");
    }

    @Test
    @DisplayName(value = "이미 MATCHED 인 매치에는 수락을 할 수 없다.")
    void acceptMatch_fail_alreadyMatched() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        MemberCreateResponse leaderC = createMember("leaderC");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamCreateResponse teamC = createTeam("teamC", leaderC.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchService.acceptMatch(match.getMatchId(), teamC.getTeamId(), leaderC.getMemberId()))
                .isInstanceOf(NotPendingTeamMatchException.class)
                .hasMessage("대기 중인 매치가 아닙니다.");
    }

    @Test
    @DisplayName(value = "존재하지 않는 매치")
    void acceptMatch_fail_notFoundMatch() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when && then
        assertThatThrownBy(() -> teamMatchService.acceptMatch(9999L, teamB.getTeamId(), leaderB.getMemberId()))
                .isInstanceOf(NotFoundTeamMatchException.class)
                .hasMessage("팀 매치 조회 실패");
    }

    @Test
    @DisplayName(value = "존재하지 않는 팀과 회원")
    void acceptMatch_fail_notFoundMember_Team() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);

        // when && then
        assertThatThrownBy(() -> teamMatchService.acceptMatch(match.getMatchId(), 999L, leaderB.getMemberId()))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");

        assertThatThrownBy(() -> teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), 1234L))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("멤버 조회 실패");
    }

    @Test
    @DisplayName(value = "여러팀의 MATCHED 매치 조회할 수 있다.")
    void getMatchedMatches() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamMatchCreateResponse match = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));


        // when
        TeamMatchAcceptResponse response = teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        List<TeamMatchMatchedResponse> matchedMatches = teamMatchService.getMatchedMatches();

        // then
        assertThat(response.getMatchId()).isEqualTo(match.getMatchId());
        assertThat(response.getStatus()).isEqualTo(TeamMatchStatus.MATCHED);
        assertThat(response.getHomeTeamId()).isEqualTo(teamA.getTeamId());
        assertThat(response.getAwayTeamId()).isEqualTo(teamB.getTeamId());

        assertThat(matchedMatches).extracting(TeamMatchMatchedResponse::getHomeTeamName).containsExactly("teamA");
        assertThat(matchedMatches).extracting(TeamMatchMatchedResponse::getAwayTeamName).containsExactly("teamB");
        assertThat(matchedMatches).extracting(TeamMatchMatchedResponse::getStatus).containsExactly(TeamMatchStatus.MATCHED);
    }

    @Test
    @DisplayName(value = "경기 시간이 가까운 순서로, 정렬된다.")
    void getMatchedMatches_orderby() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        // when
        teamMatchService.acceptMatch(matchAB.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchService.acceptMatch(matchCD.getMatchId(), teamD.getTeamId(), leaderD.getMemberId());
        List<TeamMatchMatchedResponse> response = teamMatchService.getMatchedMatches();

        // then
        assertThat(response.size()).isEqualTo(2);

        assertThat(response.get(0).getPlayedAt()).isEqualTo(teamMatchPlayedAt);
        assertThat(response.get(1).getPlayedAt()).isEqualTo(teamMatchPlayedAt.plusDays(1));
    }

    @Test
    @DisplayName(value = "MATCHED 매치가 존재하지않으면, 빈 데이터를 반환한다.")
    void getMatchedMatches_empty() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());

        // when
        List<TeamMatchMatchedResponse> response = teamMatchService.getMatchedMatches();

        // then
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName(value = "N+1 문제가 발생하지않으며, 1번의 쿼리로 MATCHED 매치들을 다 가지고온다. ( homeTeam,awayTeam 에 대한 정보도 같이 가지고옴 )")
    void getMatchedMatches_check_queryCount() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamMatchCreateResponse match = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(2)));
        teamMatchService.acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId()); // 매치수락

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

    @Test
    @DisplayName(value = "특정 팀이 등록한 PENDING 매치들을 조회할 수 있다.")
    void getPendingMatchesByTeamId() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        // when
        List<TeamMatchPendingResponse> teamAPendingMatches = teamMatchService.getPendingMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamAPendingMatches.size()).isEqualTo(1);
        assertThat(teamAPendingMatches.get(0).getMatchId()).isEqualTo(matchAB.getMatchId());
        assertThat(teamAPendingMatches.get(0).getHomeTeamId()).isEqualTo(teamA.getTeamId());
    }

    @Test
    @DisplayName(value = "특정 팀이 homeTeam 으로 참여한 MATCHED 상태인 매치들을 조회할 수 있다.")
    void getMatchedMatchesByTEamId_homeTeam() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));
        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD

        // when
        List<TeamMatchMatchedResponse> teamAHomeTeamMatchedMatches = teamMatchService.getMatchedMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamAHomeTeamMatchedMatches.size()).isEqualTo(1);
        assertThat(teamAHomeTeamMatchedMatches.get(0).getMatchId()).isEqualTo(matchAB.getMatchId());
        assertThat(teamAHomeTeamMatchedMatches.get(0).getHomeTeamId()).isEqualTo(teamA.getTeamId());
        assertThat(teamAHomeTeamMatchedMatches.get(0).getAwayTeamId()).isEqualTo(teamD.getTeamId());
    }

    @Test
    @DisplayName(value = "특정 팀이 awayTeam 으로 참여한 MATCHED 상태인 매치들을 조회할 수 있다.")
    void getMatchedMatchesByTEamId_awayTeam() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));
        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD

        // when
        List<TeamMatchMatchedResponse> teamDHomeTeamMatchedMatches = teamMatchService.getMatchedMatchesByTeamId(teamD.getTeamId());

        // then
        assertThat(teamDHomeTeamMatchedMatches.size()).isEqualTo(1);
        assertThat(teamDHomeTeamMatchedMatches.get(0).getMatchId()).isEqualTo(matchAB.getMatchId());
        assertThat(teamDHomeTeamMatchedMatches.get(0).getHomeTeamId()).isEqualTo(teamA.getTeamId());
        assertThat(teamDHomeTeamMatchedMatches.get(0).getAwayTeamId()).isEqualTo(teamD.getTeamId());
    }

    @Test
    @DisplayName(value = "특정 팀이 awayTeam 으로 참여한 매치와, homeTeam 으로 참여한 매치들중 MATCHED 상태인 매치들을 조회할 수 있다.")
    void getMatchedMatchesByTeamId_homeAndAway() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD
        teamMatchService.acceptMatch(matchCD.getMatchId(), teamA.getTeamId(), leaderA.getMemberId()); // teamC vs teamA

        // when
        List<TeamMatchMatchedResponse> teamAMatchesMatches = teamMatchService.getMatchedMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamAMatchesMatches.size()).isEqualTo(2);
        assertThat(teamAMatchesMatches).extracting(TeamMatchMatchedResponse::getHomeTeamId).containsExactly(teamA.getTeamId(), teamC.getTeamId());
    }

    @Test
    @DisplayName(value = "특정 팀이 홈팀으로 참가한 COMPLETED 매치들을 조회한다.")
    void getCompletedMatchesByTEamId_homeTeam() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD
        teamMatchResultService.registerMatchResult(matchAB.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1)); // 3:1 로, teamA 가 승리.

        // when
        List<TeamMatchCompletedResponse> teamACompletedMatches = teamMatchService.getCompletedMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamACompletedMatches.size()).isEqualTo(1);
        assertThat(teamACompletedMatches.get(0).getMatchId()).isEqualTo(matchAB.getMatchId());
        assertThat(teamACompletedMatches.get(0).getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
    }

    @Test
    @DisplayName(value = "특정 팀이 원정팀으로 참가한 COMPLETED 매치들을 조회한다.")
    void getCompletedMatchesByTEamId_awayTeam() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD
        teamMatchResultService.registerMatchResult(matchAB.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1)); // 3:1 로, teamA 가 승리.

        // when
        List<TeamMatchCompletedResponse> teamDCompletedMatches = teamMatchService.getCompletedMatchesByTeamId(teamD.getTeamId());

        // then
        assertThat(teamDCompletedMatches.size()).isEqualTo(1);
        assertThat(teamDCompletedMatches.get(0).getMatchId()).isEqualTo(matchAB.getMatchId());
        assertThat(teamDCompletedMatches.get(0).getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
    }

    @Test
    @DisplayName(value = "특정 팀이 참가한 무승부 COMPLETED도 포함된 매치들을 조회한다.")
    void getCompletedMatchesByTEamId_draw() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD
        teamMatchResultService.registerMatchResult(matchAB.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 1)); // 1:1 로, 무승부

        TeamMatchCreateResponse secondMatch = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        teamMatchService.acceptMatch(secondMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId()); // teamA vs teamB
        teamMatchResultService.registerMatchResult(secondMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1)); // 3:1 로, teamA 승리

        // when
        List<TeamMatchCompletedResponse> teamACompletedMatches = teamMatchService.getCompletedMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamACompletedMatches.size()).isEqualTo(2);
        assertThat(teamACompletedMatches).extracting(TeamMatchCompletedResponse::getHomeTeamId).containsExactly(teamA.getTeamId(), teamA.getTeamId());
    }

    @Test
    @DisplayName(value = "모든팀의 매치가 조회되는것이아닌, 특정팀의 매치들만 조회된다.")
    void getCompletedMatchesByTEamId_onlyTheTeam() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD
        teamMatchResultService.registerMatchResult(matchAB.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 1)); // 1:1 로, 무승부

        TeamMatchCreateResponse secondMatch = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        teamMatchService.acceptMatch(secondMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId()); // teamA vs teamB
        teamMatchResultService.registerMatchResult(secondMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1)); // 3:1 로, teamA 승리

        TeamMatchCreateResponse thirdMatch = teamMatchService.registerMatch(teamD.getTeamId(), leaderD.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt)); // teamD 매치등록
        teamMatchService.acceptMatch(thirdMatch.getMatchId(), teamA.getTeamId(), leaderA.getMemberId()); // teamD vs teamA
        teamMatchResultService.registerMatchResult(thirdMatch.getMatchId(), leaderD.getMemberId(), new TeamMatchResultCreateRequest(3, 3)); // 3:1 로, teamA 승리

        teamMatchService.acceptMatch(matchCD.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamC vs teamD
        teamMatchResultService.registerMatchResult(matchCD.getMatchId(), leaderC.getMemberId(), new TeamMatchResultCreateRequest(2, 1)); // 2:1 로, teamC 승리


        // when
        List<TeamMatchCompletedResponse> teamACompletedMatches = teamMatchService.getCompletedMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamACompletedMatches.size()).isEqualTo(3);
        assertThat(teamACompletedMatches).extracting(TeamMatchCompletedResponse::getHomeTeamId).containsExactly(teamA.getTeamId(), teamA.getTeamId(), teamD.getTeamId());
    }

    @Test
    @DisplayName(value = "존재하는 팀이지만, 매치를 진행하지 않은 팀의 매치들을 조회하면, 빈 목록이 반환된다.")
    void getMatchesByTeamId_empty_notFoundMatch() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());

        // when
        List<TeamMatchCompletedResponse> teamACompletedMatches = teamMatchService.getCompletedMatchesByTeamId(teamA.getTeamId());

        // then
        assertThat(teamACompletedMatches).isEmpty();
    }

    @Test
    @DisplayName(value = "존재하지 않은 팀의 매치들을 조회하면, 빈 목록이 반환된다.")
    void getMatchesByTeamId_empty_notFoundTeam() throws Exception {

        // when && then
        assertThatThrownBy(() -> teamMatchService.getPendingMatchesByTeamId(999L))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");

        assertThatThrownBy(() -> teamMatchService.getMatchedMatchesByTeamId(9949L))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");

        assertThatThrownBy(() -> teamMatchService.getCompletedMatchesByTeamId(9919L))
                .isInstanceOf(NotFoundTeamException.class)
                .hasMessage("팀 조회 실패");

    }


    @Test
    @DisplayName(value = "N+1 문제 해결 / 매치가 몇개든, 쿼리수는 2개로 나와야한다.")
    void getTheTeamMatches_queryCount() throws Exception {
        // given
        MemberCreateResponse leaderA = memberService.signup(new MemberCreateRequest("leaderA", "1234"));
        MemberCreateResponse leaderB = memberService.signup(new MemberCreateRequest("leaderB", "1234"));
        MemberCreateResponse leaderC = memberService.signup(new MemberCreateRequest("leaderC", "1234"));
        MemberCreateResponse leaderD = memberService.signup(new MemberCreateRequest("leaderD", "1234"));

        TeamCreateResponse teamA = teamService.createTeam(new TeamCreateRequest("teamA"), leaderA.getMemberId());
        TeamCreateResponse teamB = teamService.createTeam(new TeamCreateRequest("teamB"), leaderB.getMemberId());
        TeamCreateResponse teamC = teamService.createTeam(new TeamCreateRequest("teamC"), leaderC.getMemberId());
        TeamCreateResponse teamD = teamService.createTeam(new TeamCreateRequest("teamD"), leaderD.getMemberId());

        TeamMatchCreateResponse matchAB = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        TeamMatchCreateResponse matchCD = teamMatchService.registerMatch(teamC.getTeamId(), leaderC.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt.plusDays(1)));

        teamMatchService.acceptMatch(matchAB.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamA vs teamD
        teamMatchResultService.registerMatchResult(matchAB.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 1)); // 1:1 로, 무승부

        TeamMatchCreateResponse secondMatch = teamMatchService.registerMatch(teamA.getTeamId(), leaderA.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt));
        teamMatchService.acceptMatch(secondMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId()); // teamA vs teamB
        teamMatchResultService.registerMatchResult(secondMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1)); // 3:1 로, teamA 승리

        TeamMatchCreateResponse thirdMatch = teamMatchService.registerMatch(teamD.getTeamId(), leaderD.getMemberId(), new TeamMatchCreateRequest(teamMatchPlayedAt)); // teamD 매치등록
        teamMatchService.acceptMatch(thirdMatch.getMatchId(), teamA.getTeamId(), leaderA.getMemberId()); // teamD vs teamA
        teamMatchResultService.registerMatchResult(thirdMatch.getMatchId(), leaderD.getMemberId(), new TeamMatchResultCreateRequest(3, 3)); // 3:1 로, teamA 승리

        teamMatchService.acceptMatch(matchCD.getMatchId(), teamD.getTeamId(), leaderD.getMemberId()); // teamC vs teamD
        teamMatchResultService.registerMatchResult(matchCD.getMatchId(), leaderC.getMemberId(), new TeamMatchResultCreateRequest(2, 1)); // 2:1 로, teamC 승리

        // teamA -> 3개의 매치를 참여. ( 모두 종료 )

        em.flush();
        em.clear();

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        statistics.clear();

        List<TeamMatchCompletedResponse> responses = teamMatchService.getCompletedMatchesByTeamId(teamA.getTeamId());

        long queryCount = statistics.getPrepareStatementCount();

        assertThat(responses).hasSize(3);
        assertThat(queryCount).isEqualTo(2);

    }















}