package io.github.qlrxn1152.footballv3.team.service.impl;

import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.team.domain.Team;
import io.github.qlrxn1152.footballv3.team.domain.TeamRole;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.DuplicateTeamNameException;
import io.github.qlrxn1152.footballv3.team.exception.exceptions.TeamNameLengthException;
import io.github.qlrxn1152.footballv3.team.repository.TeamRepository;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import io.github.qlrxn1152.footballv3.teammember.domain.TeamMember;
import io.github.qlrxn1152.footballv3.teammember.exception.exceptions.AlreadyJoinedTeamException;
import io.github.qlrxn1152.footballv3.teammember.repository.TeamMemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamServiceImplTest {

    @Autowired private TeamService teamService;
    @Autowired private MemberService memberService;

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;

    @Test
    @DisplayName(value = "팀 생성")
    void createTeam() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when
        TeamCreateResponse teamResponse = teamService.createTeam(new TeamCreateRequest("teamA"), memberResponse.getMemberId());
        Team savedTeam = teamRepository.findById(teamResponse.getTeamId()).get();
        TeamMember savedTeamMember = teamMemberRepository.findByMemberId(memberResponse.getMemberId()).get();

        // then
        assertThat(teamResponse).isNotNull();
        assertThat(teamResponse.getTeamId()).isNotNull();
        assertThat(teamResponse.getTeamName()).isNotNull();

        assertThat(savedTeam.getId()).isEqualTo(teamResponse.getTeamId());
        assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(memberResponse.getMemberId());
        assertThat(teamMemberRepository.existsByMemberId(memberResponse.getMemberId())).isTrue();
        assertThat(savedTeamMember.getRole()).isEqualTo(TeamRole.LEADER);
        assertThat(savedTeamMember.getTeam().getId()).isEqualTo(savedTeam.getId());
    }

    @Test
    @DisplayName(value = "팀생성_팀 이름 공백제거 확인")
    void createTeam_strip() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when
        TeamCreateResponse teamResponse = teamService.createTeam(new TeamCreateRequest("   teamA   "), memberResponse.getMemberId());
        Team savedTeam = teamRepository.findById(teamResponse.getTeamId()).get();
        TeamMember savedTeamMember = teamMemberRepository.findByMemberId(memberResponse.getMemberId()).get();

        // then
        assertThat(teamResponse).isNotNull();
        assertThat(teamResponse.getTeamId()).isNotNull();
        assertThat(teamResponse.getTeamName()).isNotNull();

        assertThat(savedTeam.getId()).isEqualTo(teamResponse.getTeamId());
        assertThat(savedTeam.getLeaderMember().getId()).isEqualTo(memberResponse.getMemberId());
        assertThat(savedTeam.getTeamName()).isEqualTo("teamA");
        assertThat(teamMemberRepository.existsByMemberId(memberResponse.getMemberId())).isTrue();
        assertThat(savedTeamMember.getRole()).isEqualTo(TeamRole.LEADER);
        assertThat(savedTeamMember.getTeam().getId()).isEqualTo(savedTeam.getId());
    }

    @Test
    @DisplayName(value = "팀 생성 실패_이미 팀에 가입")
    void createTeam_fail_alreadyJoinTeam() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));
        teamService.createTeam(new TeamCreateRequest("teamA"), memberResponse.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("teamB"), memberResponse.getMemberId()))
                .isInstanceOf(AlreadyJoinedTeamException.class)
                .hasMessage("이미 팀에 속한 회원입니다.");
    }

    @Test
    @DisplayName(value = "팀 생성 실패_팀 이름 이미존재")
    void createTeam_fail_duplicateTeamName() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));
        MemberCreateResponse memberResponseB = memberService.signup(new MemberCreateRequest("userB", "1234"));
        teamService.createTeam(new TeamCreateRequest("teamA"), memberResponse.getMemberId());

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("teamA"), memberResponseB.getMemberId()))
                .isInstanceOf(DuplicateTeamNameException.class)
                .hasMessage("팀 이름 중복");
    }

    @Test
    @DisplayName(value = "팀 생성 실패_팀 이름 길이 미충족(부족)")
    void createTeam_fail_teamNameLength() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("a"), memberResponse.getMemberId()))
                .isInstanceOf(TeamNameLengthException.class)
                .hasMessage("팀 이름은 2~10글자까지만 가능합니다.");
    }

    @Test
    @DisplayName(value = "팀 생성 실패_팀 이름 길이 미충족(넘음)")
    void createTeam_fail_teamNameLength2() throws Exception {
        // given
        MemberCreateResponse memberResponse = memberService.signup(new MemberCreateRequest("userA", "1234"));

        // when && then
        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest("aasdfasgdasdrfasdg"), memberResponse.getMemberId()))
                .isInstanceOf(TeamNameLengthException.class)
                .hasMessage("팀 이름은 2~10글자까지만 가능합니다.");
    }




}