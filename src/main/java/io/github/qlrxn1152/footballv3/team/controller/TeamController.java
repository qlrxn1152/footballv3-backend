package io.github.qlrxn1152.footballv3.team.controller;

import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamCreateResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamDetailResponse;
import io.github.qlrxn1152.footballv3.team.dto.response.TeamListResponse;
import io.github.qlrxn1152.footballv3.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/api/teams")
    public ResponseEntity<TeamCreateResponse> createTeam(@Valid @RequestBody TeamCreateRequest request, @AuthenticationPrincipal Jwt jwt) {
        TeamCreateResponse response = teamService.createTeam(request, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/teams")
    public ResponseEntity<List<TeamListResponse>> getTeams() {
        List<TeamListResponse> response = teamService.getTeams();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/api/teams/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeam(@PathVariable Long teamId) {
        TeamDetailResponse response = teamService.getTeam(teamId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
