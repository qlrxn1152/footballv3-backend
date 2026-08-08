package io.github.qlrxn1152.footballv3.team;

import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamFixture;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamDetailResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamListResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrateTest
public class TeamQueryServiceTest {

    @Autowired private TeamService teamService;

    @Autowired private MemberFixture memberFixture;
    @Autowired private TeamFixture teamFixture;

    @Nested
    class FindTeams {

        @Test
        @DisplayName(value = "만들어져 있는 팀들을 최신에 만들어진 팀 순서로 확인할 수 있다.")
        void getTeams() throws Exception {
            // given
            MemberCreateResponse userA = memberFixture.signupMember("userA", "1234");
            MemberCreateResponse userB = memberFixture.signupMember("userB", "1234");
            teamFixture.createTeam("teamA", userA.getMemberId());
            teamFixture.createTeam("teamB", userB.getMemberId());

            // when
            List<TeamListResponse> teams = teamService.getTeams();

            // then
            assertThat(teams).hasSize(2);
            assertThat(teams).extracting(TeamListResponse::getTeamName).containsExactly("teamB", "teamA");
        }

        @Test
        @DisplayName(value = "팀이 존재하지 않은경우, 빈 리스트를 반환한다.")
        void getTeams_empty() throws Exception {

            // when
            List<TeamListResponse> teams = teamService.getTeams();

            // then
            assertThat(teams).isEmpty();
        }


    }

    @Nested
    class GetTheTeam {

        @Autowired private EntityManager em;
        @Autowired private EntityManagerFactory emf;

        @Test
        @DisplayName(value = "특정 팀을 조회할 수 있다.")
        void getTeam() throws Exception {
            // given
            MemberCreateResponse userA = memberFixture.signupMember("userA", "1234");
            TeamCreateResponse teamA = teamFixture.createTeam("teamA", userA.getMemberId());

            // when
            TeamDetailResponse response = teamService.getTeam(teamA.getTeamId());

            // then
            assertThat(response.getTeamId()).isEqualTo(teamA.getTeamId());
            assertThat(response.getLeaderMemberId()).isEqualTo(userA.getMemberId());
        }

        @Test
        @DisplayName(value = "특정 팀이 존재하지 않을경우, 팀 조회에 실패한다.")
        void getTeam_fail_notFoundTeam() throws Exception {
            // when && then
            assertThatThrownBy(() -> teamService.getTeam(1234L))
                    .isInstanceOf(NotFoundTeamException.class)
                    .hasMessage("팀 조회 실패");
        }

        @Test
        @DisplayName(value = "팀 상세 조회시, 멤버때문에 N + 1 문제가 발생하지 않는다.")
        void getTeam_check_query_count() throws Exception {
            MemberCreateResponse userA = memberFixture.signupMember("userA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", userA.getMemberId());

            em.flush();
            em.clear();

            SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
            Statistics statistics = sessionFactory.getStatistics();

            statistics.clear();

            teamService.getTeam(team.getTeamId());

            long queryCount = statistics.getPrepareStatementCount();

            assertThat(queryCount).isEqualTo(2);
        }


    }
}
