package io.github.qlrxn1152.footballv3.team;

import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamFixture;
import io.github.qlrxn1152.footballv3.support.fixture.TeamJoinFixture;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamNameChangeRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamLeaderTransferResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.NotTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.SameTeamLeaderException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.NotTeamMemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrateTest
public class TeamLeaderTransferServiceTest {

    @Autowired private TeamFixture teamFixture;
    @Autowired private MemberFixture memberFixture;
    @Autowired private TeamJoinFixture teamJoinFixture;

    @Autowired private TeamService teamService;
    @Autowired private TeamRepository teamRepository;

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
        assertThatCode(() -> teamService.changeTeamName(team.getTeamId(), newLeaderMember.getMemberId(), new TeamNameChangeRequest("teamB"))).doesNotThrowAnyException();
    }
}
