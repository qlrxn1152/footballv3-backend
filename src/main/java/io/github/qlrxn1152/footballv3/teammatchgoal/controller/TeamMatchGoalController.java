package io.github.qlrxn1152.footballv3.teammatchgoal.controller;

import io.github.qlrxn1152.footballv3.teammatchgoal.dto.request.TeamMatchGoalCreateRequest;
import io.github.qlrxn1152.footballv3.teammatchgoal.dto.response.TeamMatchGoalCreateResponse;
import io.github.qlrxn1152.footballv3.teammatchgoal.service.TeamMatchGoalService;
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

@RestController
@Slf4j
@RequiredArgsConstructor
public class TeamMatchGoalController {

    private final TeamMatchGoalService teamMatchGoalService;

    // 홈팀 팀장 전용
    @PostMapping("/api/matches/{matchId}/goals")
    public ResponseEntity<TeamMatchGoalCreateResponse> registerGoals(@PathVariable Long matchId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TeamMatchGoalCreateRequest request) {
        TeamMatchGoalCreateResponse response = teamMatchGoalService.registerGoals(matchId, Long.valueOf(jwt.getSubject()), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
