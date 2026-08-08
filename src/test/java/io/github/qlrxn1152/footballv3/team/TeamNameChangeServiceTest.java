package io.github.qlrxn1152.footballv3.team;

import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamJoinFixture;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamNameChangeRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.DuplicateTeamNameException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotFoundTeamException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.SameTeamNameException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions.NotSameTeamException;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotJoinedTeamException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@IntegrateTest
public class TeamNameChangeServiceTest {

    @Autowired private TeamService teamService;

    @Autowired private MemberFixture memberFixture;
    @Autowired private TeamFixture teamFixture;
    @Autowired private TeamJoinFixture teamJoinFixture;


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
