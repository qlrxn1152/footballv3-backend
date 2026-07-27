package io.github.qlrxn1152.footballv3.teammember.controller;

import io.github.qlrxn1152.footballv3.teammember.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @DeleteMapping("/api/teams/{teamId}/members/me")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt) {
        teamMemberService.leaveTeam(teamId, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
