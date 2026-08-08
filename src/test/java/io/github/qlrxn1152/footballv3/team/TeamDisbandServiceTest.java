package io.github.qlrxn1152.footballv3.team;

import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamJoinFixture;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@IntegrateTest
public class TeamDisbandServiceTest {

    @Autowired private TeamService teamService;

    @Autowired private MemberFixture memberFixture;
    @Autowired private TeamFixture teamFixture;
    @Autowired private TeamJoinFixture teamJoinFixture;


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
