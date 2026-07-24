package io.github.qlrxn1152.footballv3.teamjoinrequest.controller;

import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestApproveResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestListResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.dto.response.TeamJoinRequestResponse;
import io.github.qlrxn1152.footballv3.teamjoinrequest.service.TeamJoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TeamJoinRequestController {

    private final TeamJoinRequestService teamJoinRequestService;

    @PostMapping("/api/teams/{teamId}/join-requests")
    public ResponseEntity<TeamJoinRequestResponse> createJoinRequest(@PathVariable Long teamId, @AuthenticationPrincipal Jwt jwt) {
        TeamJoinRequestResponse response = teamJoinRequestService.createJoinRequest(teamId, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/teams/{teamId}/join-requests")
    public ResponseEntity<TeamJoinRequestListResponse> getJoinRequests(@AuthenticationPrincipal Jwt jwt, @PathVariable Long teamId) {
        TeamJoinRequestListResponse response = teamJoinRequestService.getJoinRequests(teamId, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/api/teams/{teamId}/join-requests/{requestId}/approve")
    public ResponseEntity<TeamJoinRequestApproveResponse> approveJoinRequest(@PathVariable Long teamId, @PathVariable Long requestId, @AuthenticationPrincipal Jwt jwt) {
        TeamJoinRequestApproveResponse response = teamJoinRequestService.approveJoinRequest(teamId, Long.valueOf(jwt.getSubject()), requestId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
