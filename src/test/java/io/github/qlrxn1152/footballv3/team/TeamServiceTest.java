package io.github.qlrxn1152.footballv3.team;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamJoinFixture;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamNameChangeRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamDetailResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamLeaderTransferResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamListResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.*;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IntegrateTest
public class TeamServiceTest {

    @Autowired private TeamService teamService;
    @Autowired private TeamJoinRequestService teamJoinRequestService;

    @Autowired private MemberFixture memberFixture;
    @Autowired private TeamFixture teamFixture;
    @Autowired private TeamJoinFixture teamJoinFixture;

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;

    @Nested
    class CreateTeam {

        @Test
        @DisplayName(value = "팀에 속하지 않은 회원은, 팀을 생성할 수 있다.")
        void createTeam() throws Exception {
            // given
            MemberCreateResponse leaderMember = memberFixture.signupMember("userA", "1234");

            // when
            TeamCreateResponse response = teamService.createTeam(TeamCreateRequest.of("teamA"), leaderMember.getMemberId());
            Team savedTeam = teamRepository.findById(response.getTeamId()).get();

            // then
            assertThat(response.getTeamId()).isEqualTo(savedTeam.getId());
            assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(leaderMember.getMemberId());
            assertThat(savedTeam.getTeamName()).isEqualTo("teamA");
            assertThat(teamMemberRepository.findByMemberId(leaderMember.getMemberId()).get().getTeam().getId()).isEqualTo(savedTeam.getId());
        }

        @Test
        @DisplayName(value = "팀 이름에는 앞뒤공백은 제거되고, 중간에는 공백이 포함될 수 있다.")
        void createTeam_can_middle_empty() throws Exception {
            // given
            MemberCreateResponse leaderMember = memberFixture.signupMember("userA", "1234");

            // when
            TeamCreateResponse response = teamService.createTeam(TeamCreateRequest.of("  Mo United "), leaderMember.getMemberId());
            Team savedTeam = teamRepository.findById(response.getTeamId()).get();

            // then
            assertThat(response.getTeamId()).isEqualTo(savedTeam.getId());
            assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(leaderMember.getMemberId());
            assertThat(savedTeam.getTeamName()).isEqualTo("Mo United");
            assertThat(teamMemberRepository.findByMemberId(leaderMember.getMemberId()).get().getTeam().getId()).isEqualTo(savedTeam.getId());
        }

        @Test
        @DisplayName(value = "팀에 속한 회원은, 팀을 생성할 수 없다.")
        void createTeam_fail_already_joinedTeam() throws Exception {
            // given
            MemberCreateResponse leaderMember = memberFixture.signupMember("userA", "1234");
            teamFixture.createTeam("teamA", leaderMember.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.createTeam(TeamCreateRequest.of("teamA"), leaderMember.getMemberId()))
                    .isInstanceOf(AlreadyJoinedTeamException.class)
                    .hasMessage("이미 팀에 속한 회원입니다.");
        }

        @Test
        @DisplayName(value = "팀 이름은 중복될 수 없다.")
        void createTeam_fail_duplicateTeamName() throws Exception {
            // given
            MemberCreateResponse leaderMember = memberFixture.signupMember("userA", "1234");
            MemberCreateResponse leaderMemberB = memberFixture.signupMember("userB", "1234");
            teamFixture.createTeam("teamA", leaderMember.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.createTeam(TeamCreateRequest.of("teamA"), leaderMemberB.getMemberId()))
                    .isInstanceOf(DuplicateTeamNameException.class)
                    .hasMessage("팀 이름 중복");
        }

        @Test
        @DisplayName(value = "팀 이름이 2글자 미만이면, 팀 생성에 실패해야한다.")
        void createTeam_fail_invalid_teamName_length_short() throws Exception {
            // given
            MemberCreateResponse leaderMember = memberFixture.signupMember("userA", "1234");

            // when && then
            assertThatThrownBy(() -> teamService.createTeam(TeamCreateRequest.of("t"), leaderMember.getMemberId()))
                    .isInstanceOf(TeamNameLengthException.class)
                    .hasMessage("팀 이름은 2~10글자까지만 가능합니다.");
        }

        @Test
        @DisplayName(value = "팀 이름이 10글자 초과면, 팀 생성에 실패해야한다.")
        void createTeam_fail_invalid_teamName_length_long() throws Exception {
            // given
            MemberCreateResponse leaderMember = memberFixture.signupMember("userA", "1234");

            // when && then
            assertThatThrownBy(() -> teamService.createTeam(TeamCreateRequest.of("Manchester United"), leaderMember.getMemberId()))
                    .isInstanceOf(TeamNameLengthException.class)
                    .hasMessage("팀 이름은 2~10글자까지만 가능합니다.");
        }


    }

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

    @Nested
    class transferTeamLeader{

        @Test
        @DisplayName(value = "팀의 팀장은 다른팀원에게 팀장을 넘길 수 있다.")
        void transfer_teamLeader() throws Exception {
            // given
            MemberCreateResponse oldLeader = memberFixture.signupMember("oldLeader", "1234");
            MemberCreateResponse newLeader = memberFixture.signupMember("newLeader", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", oldLeader.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), oldLeader.getMemberId(), newLeader.getMemberId());

            // when
            TeamLeaderTransferResponse response = teamService.transferTeamLeader(team.getTeamId(), oldLeader.getMemberId(), newLeader.getMemberId());
            Team savedTeam = teamRepository.findById(team.getTeamId()).get();

            // then
            assertThat(response.getTeamId()).isEqualTo(team.getTeamId());
            assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(newLeader.getMemberId());
            assertThat(savedTeam.getId()).isEqualTo(team.getTeamId());
        }

        @Test
        @DisplayName(value = "팀장이 아닌 회원은 팀장 변경에 실패해야한다.")
        void transferTeamLeader_fail_notTeamLeader() throws Exception {
            // given
            MemberCreateResponse oldLeader = memberFixture.signupMember("oldLeader", "1234");
            MemberCreateResponse newLeader = memberFixture.signupMember("newLeader", "1234");
            MemberCreateResponse memberA = memberFixture.signupMember("memberA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", oldLeader.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), oldLeader.getMemberId(), memberA.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), oldLeader.getMemberId(), newLeader.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), memberA.getMemberId(), newLeader.getMemberId()))
                    .isInstanceOf(NotTeamLeaderException.class)
                    .hasMessage("팀장이 아닙니다.");
        }

        @Test
        @DisplayName(value = "자기 자신에게 팀장을 위임할 수 없다.")
        void transferTeamLeader_fail_self() throws Exception {
            // given
            MemberCreateResponse teamLeader = memberFixture.signupMember("teamLeader", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", teamLeader.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), teamLeader.getMemberId(), teamLeader.getMemberId()))
                    .isInstanceOf(SameTeamLeaderException.class)
                    .hasMessage("자기 자신에게 팀장 위임은 불가합니다.");
        }

        @Test
        @DisplayName(value = "팀에 속하지 않은 회원에게 팀장변경은 실패한다.")
        void transferTeamLeader_fail_notJoinTeamMember() throws Exception {
            // given
            MemberCreateResponse teamLeader = memberFixture.signupMember("teamLeader", "1234");
            MemberCreateResponse memberA = memberFixture.signupMember("memberA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", teamLeader.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), teamLeader.getMemberId(), memberA.getMemberId()))
                    .isInstanceOf(NotJoinedTeamException.class)
                    .hasMessage("팀에 속한 회원이 아닙니다.");
        }

        @Test
        @DisplayName(value = "해당 팀에 속하지 않은 회원에게 팀장변경은 실패한다.")
        void transferTeamLeader_fail_notJoinSameTeamMember() throws Exception {
            // given
            MemberCreateResponse teamLeader = memberFixture.signupMember("teamLeader", "1234");
            MemberCreateResponse memberA = memberFixture.signupMember("memberA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", teamLeader.getMemberId());
            teamFixture.createTeam("teamB", memberA.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.transferTeamLeader(team.getTeamId(), teamLeader.getMemberId(), memberA.getMemberId()))
                    .isInstanceOf(NotTeamMemberException.class)
                    .hasMessage("해당팀의 일반 유저가 아닙니다.");
        }

        @Test
        @DisplayName(value = "새로운 팀장은 팀장 전용 기능을 사용할 수 있다.")
        void transferTeamLeader_canTeamLeaderMethods() throws Exception {
            // given
            MemberCreateResponse oldLeaderMember = memberFixture.signupMember("oldLeader", "1234");
            MemberCreateResponse newLeaderMember = memberFixture.signupMember("newLaeder", "1234");

            TeamCreateResponse team = teamFixture.createTeam("teamA", oldLeaderMember.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), oldLeaderMember.getMemberId(), newLeaderMember.getMemberId());

            // when
            teamService.transferTeamLeader(team.getTeamId(), oldLeaderMember.getMemberId(), newLeaderMember.getMemberId()); // 팀장변경

            // then
            assertThatCode(() -> teamJoinRequestService.getJoinRequests(team.getTeamId(), newLeaderMember.getMemberId())).doesNotThrowAnyException();
            assertThatCode(() -> teamService.transferTeamLeader(team.getTeamId(), newLeaderMember.getMemberId(), oldLeaderMember.getMemberId())).doesNotThrowAnyException();

            assertThatThrownBy(() -> teamJoinRequestService.getJoinRequests(team.getTeamId(), newLeaderMember.getMemberId()))
                    .isInstanceOf(NotTeamLeaderException.class)
                    .hasMessage("팀장이 아닙니다.");
        }





    }

    @Nested
    class disbandTeam {

        @Test
        @DisplayName(value = "팀장은 팀원이 본인외에 없을경우, 팀을 해체할 수 있다.")
        void disbandTeam() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when
            teamService.disbandTeam(team.getTeamId(), leader.getMemberId());

            // then
            assertThatThrownBy(() -> teamService.getTeam(team.getTeamId()))
                    .isInstanceOf(NotFoundTeamException.class)
                    .hasMessage("팀 조회 실패");
        }

        @Test
        @DisplayName(value = "팀장이 아닌 일반회원은 팀 해체를 진행할 수 없다.")
        void disbandTeam_fail_notTeamLeader() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse user = memberFixture.signupMember("userA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), leader.getMemberId(), user.getMemberId());

            // when
            assertThatThrownBy(() -> teamService.disbandTeam(team.getTeamId(), user.getMemberId()))
                    .isInstanceOf(NotTeamLeaderException.class)
                    .hasMessage("팀장이 아닙니다.");

            assertThat(teamService.getTeam(team.getTeamId()).getTeamId()).isEqualTo(team.getTeamId());
        }

        @Test
        @DisplayName(value = "팀에 속하지 않은 회원은 팀 해체를 진행할 수 없다.")
        void disbandTeam_fail_notTeamMember() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse user = memberFixture.signupMember("userA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when
            assertThatThrownBy(() -> teamService.disbandTeam(team.getTeamId(), user.getMemberId()))
                    .isInstanceOf(NotTeamLeaderException.class)
                    .hasMessage("팀장이 아닙니다.");

            assertThat(teamService.getTeam(team.getTeamId()).getTeamId()).isEqualTo(team.getTeamId());
        }

        @Test
        @DisplayName(value = "존재하지 않는 팀은 해체할 수 없다.")
        void disbandTeam_fail_not_exist_team() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");

            // when
            assertThatThrownBy(() -> teamService.disbandTeam(1234L, leader.getMemberId()))
                    .isInstanceOf(NotFoundTeamException.class)
                    .hasMessage("팀 조회 실패");
        }

        @Test
        @DisplayName(value = "존재하지 않는 회원은 팀을 해체할 수 없다.")
        void disbandTeam_fail_not_exist_member() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when
            assertThatThrownBy(() -> teamService.disbandTeam(team.getTeamId(), 1234L))
                    .isInstanceOf(NotFoundMemberException.class)
                    .hasMessage("멤버 조회 실패");
        }

        @Test
        @DisplayName(value = "팀 해체기능은 다른팀에 영향을 미치지 않는다.")
        void disbandTeam_keepOtherTeam() throws Exception {
            // given
            MemberCreateResponse leaderA = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leaderA.getMemberId());

            MemberCreateResponse leaderB = memberFixture.signupMember("leaderB", "1234");
            TeamCreateResponse teamB = teamFixture.createTeam("teamB", leaderB.getMemberId());

            // when
            teamService.disbandTeam(team.getTeamId(), leaderA.getMemberId());

            // then
            assertThat(teamService.getTeam(teamB.getTeamId()).getTeamId()).isEqualTo(teamB.getTeamId());
            assertThat(teamService.getTeams().size()).isEqualTo(1);
            assertThat(teamService.getTeams().get(0).getTeamId()).isEqualTo(teamB.getTeamId());
        }

        @Test
        @DisplayName(value = "팀 해체이후, 팀을 다시 생성할 수 있다.")
        void disbandTeam_afterTransferLeader_newCreateTeam() throws Exception {
            // given
            MemberCreateResponse leaderA = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leaderA.getMemberId());

            // when
            teamService.disbandTeam(team.getTeamId(), leaderA.getMemberId());

            // then
            assertThatCode(() -> teamFixture.createTeam("new TeamA", leaderA.getMemberId())).doesNotThrowAnyException();
            assertThat(teamService.getTeams().get(0).getTeamName()).isEqualTo("new TeamA");
        }

        @Test
        @DisplayName(value = "팀 해체이후, 존재하는 팀에 가입신청을 넣을 수 있다.")
        void disbandTeam_afterTransferLeader_newCreateTeam_joinRequest() throws Exception {
            // given
            MemberCreateResponse leaderA = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse leaderB = memberFixture.signupMember("leaderB", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leaderA.getMemberId());
            TeamCreateResponse teamB = teamFixture.createTeam("teamB", leaderB.getMemberId());

            // when
            teamService.disbandTeam(team.getTeamId(), leaderA.getMemberId());

            // then
            assertThatCode(() -> teamJoinFixture.joinTheTeam(teamB.getTeamId(), leaderB.getMemberId(), leaderA.getMemberId())).doesNotThrowAnyException();
        }









    }

    @Nested
    class changeTeamName {

        @Test
        @DisplayName(value = "팀장은 팀 이름을 변경할 수 있다.")
        void changeTeamName() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when
            teamService.changeTeamName(team.getTeamId(), leader.getMemberId(), new TeamNameChangeRequest("new TeamA"));

            // then
            assertThat(teamService.getTeam(team.getTeamId()).getTeamName()).isEqualTo("new TeamA");
        }

        @Test
        @DisplayName(value = "팀 이름은 앞뒤 공백을 제거한채 반영된다.")
        void changeTeamName_strip() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when
            teamService.changeTeamName(team.getTeamId(), leader.getMemberId(), new TeamNameChangeRequest("  new TeamA "));

            // then
            assertThat(teamService.getTeam(team.getTeamId()).getTeamName()).isEqualTo("new TeamA");
        }

        @Test
        @DisplayName(value = "팀장이 아닌 일반회원은 팀 이름 변경에 실패해야한다.")
        void changeTeamName_fail_not_team_leader() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse user = memberFixture.signupMember("userA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), leader.getMemberId(), user.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(team.getTeamId(), user.getMemberId(), new TeamNameChangeRequest("  new TeamA ")))
                    .isInstanceOf(NotTeamLeaderException.class)
                    .hasMessage("팀장이 아닙니다.");
        }

        @Test
        @DisplayName(value = "팀에 속하지 않은, 외부회원은 팀 이름 변경에 실패해야한다.")
        void changeTeamName_fail_not_team_member() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse user = memberFixture.signupMember("userA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(team.getTeamId(), user.getMemberId(), new TeamNameChangeRequest("  new TeamA ")))
                    .isInstanceOf(NotJoinedTeamException.class)
                    .hasMessage("팀에 속한 회원이 아닙니다.");
        }

        @Test
        @DisplayName(value = "해당팀 팀장이 아닌 다른팀 팀장인경우, 해당팀의 팀 이름 변경에 실패해야한다.")
        void changeTeamName_fail_not_sameTeam_leader() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse leaderB = memberFixture.signupMember("leaderB", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());
            teamFixture.createTeam("teamB", leaderB.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(team.getTeamId(), leaderB.getMemberId(), new TeamNameChangeRequest("  new TeamA ")))
                    .isInstanceOf(NotSameTeamException.class)
                    .hasMessage("해당팀이 아닙니다.");
        }

        @Test
        @DisplayName(value = "현재 팀 이름과 같은 팀 이름으로 변경시, 팀 이름 변경에 실패해야한다.")
        void changeTeamName_fail_same_teamName() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(team.getTeamId(), leader.getMemberId(), new TeamNameChangeRequest("teamA")))
                    .isInstanceOf(SameTeamNameException.class)
                    .hasMessage("현재 팀 이름과 동일합니다.");
        }

        @Test
        @DisplayName(value = "이미 존재하는 팀 이름에는 변경에 실패해야한다.")
        void changeTeamName_fail_duplicateTeamName() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse leaderB = memberFixture.signupMember("leaderB", "1234");

            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());
            teamFixture.createTeam("teamB", leaderB.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(team.getTeamId(), leader.getMemberId(), new TeamNameChangeRequest("teamB")))
                    .isInstanceOf(DuplicateTeamNameException.class)
                    .hasMessage("팀 이름 중복");
        }

        @Test
        @DisplayName(value = "존재하지 않는 팀 이름 변경시, 실패한다.")
        void changeTeamName_fail_notExistTeam() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");


            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(1234L, leader.getMemberId(), new TeamNameChangeRequest("teamB")))
                    .isInstanceOf(NotFoundTeamException.class)
                    .hasMessage("팀 조회 실패");
        }

        @Test
        @DisplayName(value = "존재하지 않는 팀 이름 변경시, 실패한다.")
        void changeTeamName_fail_notExistMember() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());

            // when && then
            assertThatThrownBy(() -> teamService.changeTeamName(team.getTeamId(), 1234L, new TeamNameChangeRequest("teamB")))
                    .isInstanceOf(NotFoundMemberException.class)
                    .hasMessage("멤버 조회 실패");
        }

        @Test
        @DisplayName(value = "새로운 팀장은 팀 이름을 변경할 수 있다.")
        void changeTeamName_newTeamLeader() throws Exception {
            // given
            MemberCreateResponse leader = memberFixture.signupMember("leaderA", "1234");
            MemberCreateResponse newLeader = memberFixture.signupMember("newLeader", "1234");
            TeamCreateResponse team = teamFixture.createTeam("teamA", leader.getMemberId());
            teamJoinFixture.joinTheTeam(team.getTeamId(), leader.getMemberId(), newLeader.getMemberId());

            teamService.transferTeamLeader(team.getTeamId(), leader.getMemberId(), newLeader.getMemberId());

            // when && then
            assertThatCode(() -> teamService.changeTeamName(team.getTeamId(), newLeader.getMemberId(), new TeamNameChangeRequest("teamB")))
                    .doesNotThrowAnyException();
            assertThat(teamService.getTeam(team.getTeamId()).getTeamName()).isEqualTo("teamB");
        }




    }
}
