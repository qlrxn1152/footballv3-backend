package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchAcceptResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCompletedResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchResultResponse;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.AlreadyExistTeamMatchResultException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.InvalidTeamMatchScoreException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.NotFoundTeamMatchException;
import io.github.qlrxn1152.footballv3.teammatch.exception.exceptions.NotMatchedTeamMatchException;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchRepository;
import io.github.qlrxn1152.footballv3.teammatch.repository.TeamMatchResultRepository;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchResultService;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchService;
import io.github.qlrxn1152.footballv3.teammatchgoal.domain.TeamMatchGoal;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalCreateRequest;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalScorerRequest;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.response.TeamMatchGoalCreateResponse;
import io.github.qlrxn1152.footballv3.teammatchgoal.service.TeamMatchGoalService;
import io.github.qlrxn1152.footballv3.teammatchgoal.service.impl.TeamMatchGoalServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamMatchResultServiceImplTest {

    @Autowired private TeamMatchService teamMatchService;
    @Autowired private TeamService teamService;
    @Autowired private MemberService memberService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;
    @Autowired private TeamMatchResultService teamMatchResultService;
    @Autowired private TeamMatchGoalService teamMatchGoalService;

    @Autowired private TeamMatchRepository teamMatchRepository;
    @Autowired private TeamMatchResultRepository teamMatchResultRepository;
    @Autowired private TeamRepository teamRepository;

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

    private TeamMatchAcceptResponse acceptMatch(Long matchId, Long awayTeamId, Long loginMemberId) {
        return teamMatchService.acceptMatch(matchId, awayTeamId, loginMemberId);
    }

    private List<TeamMatchGoalScorerRequest> homeScorer(int goalCount) {
        TeamMatchGoalScorerRequest scorerHomeA = new TeamMatchGoalScorerRequest(createMember("homeA").getMemberId(), 1);
        TeamMatchGoalScorerRequest scorerHomeB = new TeamMatchGoalScorerRequest(createMember("homeB").getMemberId(), goalCount - 1);
        TeamMatchGoalScorerRequest scorerHomeC = new TeamMatchGoalScorerRequest(createMember("homeC").getMemberId(), 0);

        List<TeamMatchGoalScorerRequest> homeScorers = new ArrayList<>();
        homeScorers.add(scorerHomeA);
        homeScorers.add(scorerHomeB);
        homeScorers.add(scorerHomeC);

        return homeScorers;
    }

    private List<TeamMatchGoalScorerRequest> awayScorer(int goalCount) {
        TeamMatchGoalScorerRequest scorerAwayA = new TeamMatchGoalScorerRequest(createMember("awayA").getMemberId(), 1);
        TeamMatchGoalScorerRequest scorerAwayB = new TeamMatchGoalScorerRequest(createMember("awayB").getMemberId(), 0);
        TeamMatchGoalScorerRequest scorerAwayC = new TeamMatchGoalScorerRequest(createMember("awayC").getMemberId(), 0);

        List<TeamMatchGoalScorerRequest> awayScorers = new ArrayList<>();
        awayScorers.add(scorerAwayA);
        awayScorers.add(scorerAwayB);
        awayScorers.add(scorerAwayC);

        return awayScorers;
    }

    private TeamMatchGoalCreateRequest createTeamMatchGoal(int homeGoalCount, int awayGoalCount) {
        return new TeamMatchGoalCreateRequest(homeScorer(homeGoalCount), awayScorer(awayGoalCount));
    }

    @Test
    @DisplayName(value = "홈팀 팀장은 MATCHED 매치의 결과를 입력할 수 있다. ( 홈팀승 ) ")
    void registerMatchResult_homeWin() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());


        // when
        TeamMatchResultResponse response = teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 1));
        TeamMatchGoalCreateResponse matchGoalResponse = teamMatchGoalService.registerGoals(match.getMatchId(), leaderA.getMemberId(), createTeamMatchGoal(2, 1));
        TeamMatchResult teamMatchResult = teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        TeamMatch teamMatch = teamMatchRepository.findById(match.getMatchId()).get();

        // then
        assertThat(matchGoalResponse.getHomeScore()).isEqualTo(2);
        assertThat(matchGoalResponse.getAwayScore()).isEqualTo(1);

        assertThat(matchGoalResponse.getHomeScorers().size()).isEqualTo(3);
        assertThat(matchGoalResponse.getAwayScorers().size()).isEqualTo(3);

        assertThat(matchGoalResponse.getHomeScorers()).extracting(TeamMatchGoalScorerRequest::getGoalCount).containsExactly(1, 1, 0);
        assertThat(matchGoalResponse.getAwayScorers()).extracting(TeamMatchGoalScorerRequest::getGoalCount).containsExactly(1, 0, 0);

        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(match.getMatchId());
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(response.getMatchId());
        assertThat(teamMatchResult.getWinnerTeam().getId()).isEqualTo(teamA.getTeamId());
        assertThat(teamMatchResult.getHomeScore()).isEqualTo(2);
        assertThat(teamMatchResult.getAwayScore()).isEqualTo(1);

        assertThat(teamMatch.getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
        assertThat(teamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
    }

    @Test
    @DisplayName(value = "홈팀 팀장은 MATCHED 매치의 결과를 입력할 수 있다. ( 어웨이팀 승 )")
    void registerMatchResult_awayWin() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when
        TeamMatchResultResponse response = teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 2));
        TeamMatchResult teamMatchResult = teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        TeamMatch teamMatch = teamMatchRepository.findById(match.getMatchId()).get();

        // then
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(match.getMatchId());
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(response.getMatchId());
        assertThat(teamMatchResult.getWinnerTeam().getId()).isEqualTo(teamB.getTeamId());
        assertThat(teamMatchResult.getHomeScore()).isEqualTo(1);
        assertThat(teamMatchResult.getAwayScore()).isEqualTo(2);

        assertThat(teamMatch.getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
        assertThat(teamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
    }

    @Test
    @DisplayName(value = "홈팀 팀장은 MATCHED 매치의 결과를 입력할 수 있다. ( 무승부 ) ")
    void registerMatchResult_draw() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when
        TeamMatchResultResponse response = teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 1));
        TeamMatchResult teamMatchResult = teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        TeamMatch teamMatch = teamMatchRepository.findById(match.getMatchId()).get();

        // then
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(match.getMatchId());
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(response.getMatchId());
        assertThat(teamMatchResult.getWinnerTeam()).isNull();
        assertThat(teamMatchResult.getHomeScore()).isEqualTo(1);
        assertThat(teamMatchResult.getAwayScore()).isEqualTo(1);

        assertThat(teamMatch.getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
        assertThat(teamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
    }

    @Test
    @DisplayName(value = "원정팀 팀장은 결과를 입력할 수 없다.")
    void registerMatchResult_fail_awayTeamLeader() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderB.getMemberId(), new TeamMatchResultCreateRequest(2, 1)))
                .isInstanceOf(NotSameTeamException.class)
                .hasMessage("해당팀이 아닙니다.");
    }

    @Test
    @DisplayName(value = "PENDING 상태인 매치에는 결과를 입력할 수 없다.")
    void registerMatchResult_fail_pending() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt); // 매치등록

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 1)))
                .isInstanceOf(NotMatchedTeamMatchException.class)
                .hasMessage("진행 중인 매치가 아닙니다.");
    }

    @Test
    @DisplayName(value = "이미 매치결과가 존재하는 매치에는 추가로 매치결과를 입력할 수 없다.")
    void registerMatchResult_fail_duplicateMatchResult() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 1));

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 4)))
                .isInstanceOf(NotMatchedTeamMatchException.class)
                .hasMessage("진행 중인 매치가 아닙니다.");
    }

    @Test
    @DisplayName(value = "점수는 0 이상의 숫자가 들어와야한다.")
    void registerMatchResult_fail_score() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(-2, 4)))
                .isInstanceOf(InvalidTeamMatchScoreException.class)
                .hasMessage("경기 점수는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName(value = "점수는 0 이상의 숫자가 들어와야한다.")
    void registerMatchResult_fail_score_away() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, -4)))
                .isInstanceOf(InvalidTeamMatchScoreException.class)
                .hasMessage("경기 점수는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName(value = "존재하지 않는 매치")
    void registerMatchResult_fail_notFoundMatch() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(1234L, leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, -4)))
                .isInstanceOf(NotFoundTeamMatchException.class)
                .hasMessage("팀 매치 조회 실패");
    }

    @Test
    @DisplayName(value = "존재하지 않는 회원")
    void registerMatchResult_fail_notFoundMember() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), 5145L, new TeamMatchResultCreateRequest(2, -4)))
                .isInstanceOf(NotFoundMemberException.class)
                .hasMessage("멤버 조회 실패");
    }

    @Test
    @DisplayName(value = "홈팀이 승리할경우, 홈팀은 점수 +30, 원정팀은 -30이 반영된다.")
    void registerMatchResult_homeWin_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when
        TeamMatchResultResponse response = teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 1)); // 홈팀 승리
        TeamMatchResult teamMatchResult = teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        TeamMatch teamMatch = teamMatchRepository.findById(match.getMatchId()).get();
        Team teamAEntity = teamRepository.findById(teamA.getTeamId()).get();
        Team teamBEntity = teamRepository.findById(teamB.getTeamId()).get();

        // then

        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(match.getMatchId());
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(response.getMatchId());
        assertThat(teamMatchResult.getWinnerTeam().getId()).isEqualTo(teamA.getTeamId());
        assertThat(teamMatchResult.getHomeScore()).isEqualTo(2);
        assertThat(teamMatchResult.getAwayScore()).isEqualTo(1);

        assertThat(teamAEntity.getRating()).isEqualTo(1530);
        assertThat(teamBEntity.getRating()).isEqualTo(1470);

        assertThat(teamMatch.getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
        assertThat(teamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
    }

    @Test
    @DisplayName(value = "원정팀이 승리할경우, 원정팀은 점수 +30, 홈팀은 -30이 반영된다.")
    void registerMatchResult_awayWin_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when
        TeamMatchResultResponse response = teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 4)); // 원정팀 승리
        TeamMatchResult teamMatchResult = teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        TeamMatch teamMatch = teamMatchRepository.findById(match.getMatchId()).get();
        Team teamAEntity = teamRepository.findById(teamA.getTeamId()).get();
        Team teamBEntity = teamRepository.findById(teamB.getTeamId()).get();

        // then

        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(match.getMatchId());
        assertThat(teamMatchResult.getTeamMatch().getId()).isEqualTo(response.getMatchId());
        assertThat(teamMatchResult.getWinnerTeam().getId()).isEqualTo(teamB.getTeamId());
        assertThat(teamMatchResult.getHomeScore()).isEqualTo(2);
        assertThat(teamMatchResult.getAwayScore()).isEqualTo(4);

        assertThat(teamAEntity.getRating()).isEqualTo(1470);
        assertThat(teamBEntity.getRating()).isEqualTo(1530);

        assertThat(teamMatch.getStatus()).isEqualTo(TeamMatchStatus.COMPLETED);
        assertThat(teamMatch.getAwayTeam().getId()).isEqualTo(teamB.getTeamId());
    }


    @Test
    @DisplayName(value = "무승부인 경우에는, 양팀 모두 10점을 획득한다.")
    void registerMatchResult_draw_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(2, 2)); // 무승부
        teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        teamMatchRepository.findById(match.getMatchId()).get();
        Team teamAEntity = teamRepository.findById(teamA.getTeamId()).get();
        Team teamBEntity = teamRepository.findById(teamB.getTeamId()).get();

        // then
        assertThat(teamAEntity.getRating()).isEqualTo(1510);
        assertThat(teamBEntity.getRating()).isEqualTo(1510);
    }

    @Test
    @DisplayName(value = "무승부인 경우에는, 양팀 모두 10점을 획득한다. ( 0 : 0 )")
    void registerMatchResult_zeroDraw_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(0, 0)); // 무승부
        teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        teamMatchRepository.findById(match.getMatchId()).get();
        Team teamAEntity = teamRepository.findById(teamA.getTeamId()).get();
        Team teamBEntity = teamRepository.findById(teamB.getTeamId()).get();

        // then
        assertThat(teamAEntity.getRating()).isEqualTo(1510);
        assertThat(teamBEntity.getRating()).isEqualTo(1510);
    }

    @Test
    @DisplayName(value = "잘못된 점수로 매치결과를 등록하면, 매치결과등록에 실패해야하고 점수도 반영되어선 안된다.")
    void registerMatchResult_fail_negativeScore_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(-2, 2)))
                .isInstanceOf(InvalidTeamMatchScoreException.class)
                .hasMessage("경기 점수는 0 이상이어야 합니다.");

        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1500);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1500);
    }

    @Test
    @DisplayName(value = "매치결과를 등록하는데에 실패하면, 매치결과등록에 실패해야하고 점수도 반영되어선 안된다.")
    void registerMatchResult_fail_notTeamLeader_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderB.getMemberId(), new TeamMatchResultCreateRequest(1, 2)))
                .isInstanceOf(NotSameTeamException.class)
                .hasMessage("해당팀이 아닙니다.");

        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1500);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1500);
    }

    @Test
    @DisplayName(value = "이미 매치결과가 입력된 매치에 다시 결과를 입력해도, 결과가 반영되지 않아야한다.")
    void registerMatchResult_fail_duplicate_rating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 0));

        // when && then
        assertThatThrownBy(() -> teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 2)))
                .isInstanceOf(NotMatchedTeamMatchException.class)
                .hasMessage("진행 중인 매치가 아닙니다.");

        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1530);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1470);
    }

    @Test
    @DisplayName(value = "레이팅은 초기화되지않고, 계속해서 누적되어야한다.")
    void registerMatchResult_rating_accumulate() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // 1번째 경기
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 0));

        // 2번째 경기
        TeamMatchCreateResponse reMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(reMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(reMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 0));

        // when && then
        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1560);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1440);
    }

    @Test
    @DisplayName(value = "COMPLETED 인 매치들을 조회할 수 있다.")
    void getCompletedMatches() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // 1번째, 2번째 경기 모두 결과를 입력해서 종료 ( teamA vs teamB )

        // 1번째 경기
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 0));

        // 2번째 경기
        TeamMatchCreateResponse reMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(reMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(reMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 0));

        List<TeamMatchCompletedResponse> completedMatches = teamMatchService.getCompletedMatches();


        // when && then
        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1560);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1440);
        assertThat(completedMatches).hasSize(2);
        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getHomeTeamName).containsExactly("teamA", "teamA");
        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getHomeTeamRating).containsExactly(1560, 1560);
    }

    @Test
    @DisplayName(value = "COMPLETED 인 매치들을 조회할 수 있다. ( 원정팀 승리 )")
    void getCompletedMatches_awayWin() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // 1번째, 2번째 경기 모두 결과를 입력해서 종료 ( teamA vs teamB )

        // 1번째 경기
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 3)); // 1:3

        // 2번째 경기
        TeamMatchCreateResponse reMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(reMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(reMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 5)); // 3:5

        List<TeamMatchCompletedResponse> completedMatches = teamMatchService.getCompletedMatches();


        // when && then
        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1440);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1560);
        assertThat(completedMatches).hasSize(2);

        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getWinnerTeamName).containsExactly("teamB", "teamB");
        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getAwayTeamRating).containsExactly(1560, 1560);
    }

    @Test
    @DisplayName(value = "점수가 무승부인 COMPLETED 매치도 결과에 포함된다.")
    void getCompletedMatches_draw() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // 1번째, 2번째 경기 모두 결과를 입력해서 종료 ( teamA vs teamB )

        // 1번째 경기
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 1));

        // 2번째 경기
        TeamMatchCreateResponse reMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(reMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(reMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 3));

        List<TeamMatchCompletedResponse> completedMatches = teamMatchService.getCompletedMatches();


        // when && then
        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1520);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1520);
        assertThat(completedMatches).hasSize(2);

        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getWinnerTeamName).containsExactly(null, null);
        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getAwayTeamRating).containsExactly(1520, 1520);
        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getHomeTeamRating).containsExactly(1520, 1520);
    }

    @Test
    @DisplayName(value = "COMPLETED 인 매치들만 조회되어야 한다.")
    void getCompletedMatches_only_completed() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // 1번째 경기
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(1, 2)); // 원정팀 승리

        // 2번째 경기
        TeamMatchCreateResponse reMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(reMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(reMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1)); // 홈팀 승리

        // 3번째 경기 -> MATCHED
        TeamMatchCreateResponse matchedMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(matchedMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // 4번째 경기 -> PENDING
        TeamMatchCreateResponse pendingMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);


        List<TeamMatchCompletedResponse> completedMatches = teamMatchService.getCompletedMatches();


        // when && then
        assertThat(teamRepository.findById(teamA.getTeamId()).get().getRating()).isEqualTo(1500);
        assertThat(teamRepository.findById(teamB.getTeamId()).get().getRating()).isEqualTo(1500);
        assertThat(completedMatches).hasSize(2);

        assertThat(completedMatches).extracting(TeamMatchCompletedResponse::getWinnerTeamName).containsExactly("teamA", "teamB");
    }

    @Test
    @DisplayName(value = "완료된 매치가 존재하지 않으면, 빈 목록을 반환한다.")
    void getCompletedMatches_empty() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // MATCHED
        TeamMatchCreateResponse matchedMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(matchedMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());

        // PENDING
        TeamMatchCreateResponse pendingMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);


        List<TeamMatchCompletedResponse> completedMatches = teamMatchService.getCompletedMatches();


        // when && then
        assertThat(completedMatches).isEmpty();
    }

    @Test
    @DisplayName(value = "N+1 문제 확인. 조회하는데에는 join fetch 를 통해서 해결한다.")
    void getCompletedMatches_joinFetch() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());

        // 1번째, 2번째 경기 모두 결과를 입력해서 종료 ( teamA vs teamB )

        // 1번째 경기
        TeamMatchCreateResponse match = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(match.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(match.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(4, 2));

        // 2번째 경기
        TeamMatchCreateResponse reMatch = registerPendingMatch(teamA.getTeamId(), leaderA.getMemberId(), teamMatchPlayedAt);
        acceptMatch(reMatch.getMatchId(), teamB.getTeamId(), leaderB.getMemberId());
        teamMatchResultService.registerMatchResult(reMatch.getMatchId(), leaderA.getMemberId(), new TeamMatchResultCreateRequest(3, 1));

        em.flush();
        em.clear();

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);

        Statistics statistics = sessionFactory.getStatistics();

        statistics.clear();

        List<TeamMatchCompletedResponse> responses = teamMatchService.getCompletedMatches();


        long queryCount =
                statistics.getPrepareStatementCount();

        assertThat(responses)
                .hasSize(2);

        assertThat(queryCount)
                .isEqualTo(1);

    }



}