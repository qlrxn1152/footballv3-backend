package io.github.qlrxn1152.footballv3.teammatch.controller;

import com.nimbusds.jwt.JWT;
import io.github.qlrxn1152.footballv3.teammatch.dto.request.TeamMatchCreateRequest;
import io.github.qlrxn1152.footballv3.teammatch.dto.response.TeamMatchCreateResponse;
import io.github.qlrxn1152.footballv3.teammatch.service.TeamMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TeamMatchController {

    private final TeamMatchService teamMatchService;

    @PostMapping("/api/teams/{teamId}/matches")
    public ResponseEntity<TeamMatchCreateResponse> registerMatch(@PathVariable Long teamId, @Valid @RequestBody TeamMatchCreateRequest request, @AuthenticationPrincipal Jwt jwt) {
        TeamMatchCreateResponse response = teamMatchService.registerMatch(teamId, Long.valueOf(jwt.getSubject()), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
