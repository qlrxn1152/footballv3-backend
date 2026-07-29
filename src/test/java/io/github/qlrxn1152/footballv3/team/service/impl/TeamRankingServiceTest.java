package io.github.qlrxn1152.footballv3.team.service.impl;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamRankingResponse;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchResultCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchResultService;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamRankingServiceImplTest {

    @Autowired private TeamService teamService;
    @Autowired private TeamMatchService teamMatchService;
    @Autowired private TeamMatchResultService teamMatchResultService;
    @Autowired private TeamRepository teamRepository;
    @Autowired private MemberService memberService;
    @Autowired private EntityManager em;
    @Autowired private EntityManagerFactory emf;

    private final LocalDateTime playedAt = LocalDateTime.of(3000, 1, 1, 18, 0);

    private MemberCreateResponse createMember(String username) {
        return memberService.signup(new MemberCreateRequest(username, "1234"));
    }

    private TeamCreateResponse createTeam(String teamName, Long leaderId) {
        return teamService.createTeam(new TeamCreateRequest(teamName), leaderId);
    }

    private TeamMatchCreateResponse createMatchedMatch(
            Long homeTeamId,
            Long homeLeaderId,
            Long awayTeamId,
            Long awayLeaderId,
            LocalDateTime matchPlayedAt
    ) {
        TeamMatchCreateResponse match =
                teamMatchService.registerMatch(
                        homeTeamId,
                        homeLeaderId,
                        new TeamMatchCreateRequest(matchPlayedAt)
                );

        teamMatchService.acceptMatch(
                match.getMatchId(),
                awayTeamId,
                awayLeaderId
        );

        return match;
    }

    private void completeMatch(
            Long matchId,
            Long homeLeaderId,
            int homeScore,
            int awayScore
    ) {
        teamMatchResultService.registerMatchResult(
                matchId,
                homeLeaderId,
                new TeamMatchResultCreateRequest(
                        homeScore,
                        awayScore
                )
        );
    }


    @Test
    @DisplayName(value = "팀이 존재하지 않으면 랭킹은 빈 목록이다.")
    void getTEamRankings_empty() throws Exception {
        // when
        List<TeamRankingResponse> responses = teamService.getTeamRankings();

        // then
        assertThat(responses).isEmpty();
    }


    @Test
    @DisplayName(value = "팀의 레이팅이 같으면 공동순위로 나타난다. ( 레이팅이 겹칠경우, pk 인 아이디값이 우선인 팀이 위로 정렬된다.")
    void getTeamRankings_sameRating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        MemberCreateResponse leaderC = createMember("leaderC");

        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamCreateResponse teamC = createTeam("teamC", leaderC.getMemberId());

        // when
        List<TeamRankingResponse> responses = teamService.getTeamRankings();

        // then
        assertThat(responses).extracting(TeamRankingResponse::getTeamId).containsExactly(teamA.getTeamId(), teamB.getTeamId(), teamC.getTeamId());
        assertThat(responses).extracting(TeamRankingResponse::getRating).containsExactly(1500, 1500, 1500);
        assertThat(responses).extracting(TeamRankingResponse::getRank).containsExactly(1, 1, 1);
    }

    @Test
    @DisplayName(value = "경기 결과가 반영되어서, 팀 레이팅이 변한 순서대로 랭킹을 조회한다. ( 레이팅이 겹칠경우, pk 인 아이디값이 우선인 팀이 위로 정렬된다.)")
    void getTeamRankings_afterMatchResults() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        MemberCreateResponse leaderC = createMember("leaderC");
        MemberCreateResponse leaderD = createMember("leaderD");

        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamCreateResponse teamC = createTeam("teamC", leaderC.getMemberId());
        TeamCreateResponse teamD = createTeam("teamD", leaderD.getMemberId());

        // teamA vs teamB
        TeamMatchCreateResponse teamAvsTeamBMatch = createMatchedMatch(teamA.getTeamId(), leaderA.getMemberId(), teamB.getTeamId(), leaderB.getMemberId(), playedAt);

        // teamC vs teamD
        TeamMatchCreateResponse teamCvsTeamDMatch = createMatchedMatch(teamC.getTeamId(), leaderC.getMemberId(), teamD.getTeamId(), leaderD.getMemberId(), playedAt);

        // teamA vs teamB -> 무승부
        completeMatch(teamAvsTeamBMatch.getMatchId(), leaderA.getMemberId(), 1, 1);

        // teamC -> teamD -> teamC 승리
        completeMatch(teamCvsTeamDMatch.getMatchId(), leaderC.getMemberId(), 3, 1);


        // when
        List<TeamRankingResponse> responses = teamService.getTeamRankings();

        // then
        assertThat(responses).hasSize(4);
        assertThat(responses).extracting(TeamRankingResponse::getTeamId).containsExactly(teamC.getTeamId(), teamA.getTeamId(), teamB.getTeamId(), teamD.getTeamId());
        assertThat(responses).extracting(TeamRankingResponse::getRating).containsExactly(1530, 1510, 1510, 1470);
        assertThat(responses).extracting(TeamRankingResponse::getRank).containsExactly(1, 2, 2, 4);
    }

    @Test
    @DisplayName(value = "가장 최신 결과를 바탕으로 레이팅을 반영한다. ( 레이팅이 겹칠경우, pk 인 아이디값이 우선인 팀이 위로 정렬된다.)")
    void getTeamRankings_updatedAfterAnotherMatch() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        MemberCreateResponse leaderC = createMember("leaderC");
        MemberCreateResponse leaderD = createMember("leaderD");

        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamCreateResponse teamC = createTeam("teamC", leaderC.getMemberId());
        TeamCreateResponse teamD = createTeam("teamD", leaderD.getMemberId());

        // teamA vs teamB
        TeamMatchCreateResponse teamAvsTeamBMatch = createMatchedMatch(teamA.getTeamId(), leaderA.getMemberId(), teamB.getTeamId(), leaderB.getMemberId(), playedAt);

        // teamC vs teamD
        TeamMatchCreateResponse teamCvsTeamDMatch = createMatchedMatch(teamC.getTeamId(), leaderC.getMemberId(), teamD.getTeamId(), leaderD.getMemberId(), playedAt);

        // teamA vs teamC
        TeamMatchCreateResponse teamAvsTeamCMatch = createMatchedMatch(teamA.getTeamId(), leaderA.getMemberId(), teamC.getTeamId(), leaderC.getMemberId(), playedAt);


        // teamA vs teamB -> 무승부
        completeMatch(teamAvsTeamBMatch.getMatchId(), leaderA.getMemberId(), 1, 1);

        // teamC vs teamD -> teamC 승리
        completeMatch(teamCvsTeamDMatch.getMatchId(), leaderC.getMemberId(), 3, 1);

        // 1510, 1510, 1530, 1470

        // teamA vs teamC -> teamA 승리
        completeMatch(teamAvsTeamCMatch.getMatchId(), leaderA.getMemberId(), 3, 2);

        // 1540, 1510, 1500, 1470


        // when
        List<TeamRankingResponse> responses = teamService.getTeamRankings();

        // then
        assertThat(responses).hasSize(4);
        assertThat(responses).extracting(TeamRankingResponse::getTeamId).containsExactly(teamA.getTeamId(), teamB.getTeamId(), teamC.getTeamId(), teamD.getTeamId());
        assertThat(responses).extracting(TeamRankingResponse::getRating).containsExactly(1540, 1510, 1500, 1470);
        assertThat(responses).extracting(TeamRankingResponse::getRank).containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName(value = "N+1 문제가 발생하지않는다 / 조회하는데에 쿼리는 1번만 실행되어야한다.")
    void getTeamRankings_queryCount() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        MemberCreateResponse leaderB = createMember("leaderB");
        MemberCreateResponse leaderC = createMember("leaderC");
        MemberCreateResponse leaderD = createMember("leaderD");
        MemberCreateResponse leaderE = createMember("leaderE");

        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());
        TeamCreateResponse teamB = createTeam("teamB", leaderB.getMemberId());
        TeamCreateResponse teamC = createTeam("teamC", leaderC.getMemberId());
        TeamCreateResponse teamD = createTeam("teamD", leaderD.getMemberId());
        TeamCreateResponse teamE = createTeam("teamE", leaderE.getMemberId());


        // teamA vs teamB
        TeamMatchCreateResponse teamAvsTeamBMatch = createMatchedMatch(teamA.getTeamId(), leaderA.getMemberId(), teamB.getTeamId(), leaderB.getMemberId(), playedAt);

        // teamC vs teamD
        TeamMatchCreateResponse teamCvsTeamDMatch = createMatchedMatch(teamC.getTeamId(), leaderC.getMemberId(), teamD.getTeamId(), leaderD.getMemberId(), playedAt);

        // teamA vs teamC
        TeamMatchCreateResponse teamAvsTeamCMatch = createMatchedMatch(teamA.getTeamId(), leaderA.getMemberId(), teamC.getTeamId(), leaderC.getMemberId(), playedAt);


        // teamA vs teamB -> 무승부
        completeMatch(teamAvsTeamBMatch.getMatchId(), leaderA.getMemberId(), 1, 1);

        // teamC vs teamD -> teamC 승리
        completeMatch(teamCvsTeamDMatch.getMatchId(), leaderC.getMemberId(), 3, 1);

        // teamA vs teamC -> teamA 승리
        completeMatch(teamAvsTeamCMatch.getMatchId(), leaderA.getMemberId(), 3, 2);

        em.flush();
        em.clear();

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        // when
        List<TeamRankingResponse> responses = teamService.getTeamRankings();

        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(responses).hasSize(5);
        assertThat(responses).extracting(TeamRankingResponse::getTeamId).containsExactly(teamA.getTeamId(), teamB.getTeamId(), teamC.getTeamId(), teamE.getTeamId(), teamD.getTeamId());
        assertThat(responses).extracting(TeamRankingResponse::getRating).containsExactly(1540, 1510, 1500, 1500, 1470);
        assertThat(responses).extracting(TeamRankingResponse::getRank).containsExactly(1, 2, 3, 3, 5);
        assertThat(queryCount).isEqualTo(1);
    }

    @Test
    @DisplayName(value = "랭킹 조회를 한다고해서, 레이팅이 변경되지 않는다.")
    void getTeamRankings_doesNotChangeRating() throws Exception {
        // given
        MemberCreateResponse leaderA = createMember("leaderA");
        TeamCreateResponse teamA = createTeam("teamA", leaderA.getMemberId());

        // when
        List<TeamRankingResponse> responses = teamService.getTeamRankings();
        Team savedTeamA = teamRepository.findById(teamA.getTeamId()).get();

        // then
        assertThat(responses).hasSize(1);
        assertThat(savedTeamA.getRating()).isEqualTo(1500);
    }







}