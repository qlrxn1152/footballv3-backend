package io.github.qlrxn1152.footballv3.teammatch.service.impl;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatch;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchResult;
import io.github.qlrxn1152.footballv3.teammatch.domain.TeamMatchStatus;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchAcceptResponse;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Autowired private TeamMatchRepository teamMatchRepository;
    @Autowired private TeamMatchResultRepository teamMatchResultRepository;

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
        TeamMatchResult teamMatchResult = teamMatchResultRepository.findByTeamMatchId(match.getMatchId()).get();
        TeamMatch teamMatch = teamMatchRepository.findById(match.getMatchId()).get();

        // then
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
}