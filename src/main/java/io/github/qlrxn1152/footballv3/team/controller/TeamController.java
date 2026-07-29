package io.github.qlrxn1152.footballv3.team.controller;

import io.github.qlrxn1152.footballv3.team.dto.request.TeamCreateRequest;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamLeaderTransferRequest;
import io.github.qlrxn1152.footballv3.team.dto.request.TeamNameChangeRequest;
import io.github.qlrxn1152.footballv3.team.dto.response.*;
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

    @PatchMapping("/api/teams/{teamId}/leader")
    public ResponseEntity<TeamLeaderTransferResponse> transferTeamLeader(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TeamLeaderTransferRequest request) {
        TeamLeaderTransferResponse response = teamService.transferTeamLeader(teamId, Long.valueOf(jwt.getSubject()), request.getNewLeaderMemberId());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/api/teams/{teamId}")
    public ResponseEntity<Void> disbandTeam(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt) {
        teamService.disbandTeam(teamId, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/api/teams/{teamId}/name")
    public ResponseEntity<TeamNameChangeResponse> changeTeamName(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TeamNameChangeRequest request) {
        TeamNameChangeResponse response = teamService.changeTeamName(teamId, Long.valueOf(jwt.getSubject()), request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/api/teams/rankings")
    public ResponseEntity<List<TeamRankingResponse>> getTeamRankings() {
        List<TeamRankingResponse> response = teamService.getTeamRankings();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
