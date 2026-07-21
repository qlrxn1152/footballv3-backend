package io.github.qlrxn1152.footballv3.member.controller;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberMeResponse;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/api/members")
    public ResponseEntity<MemberCreateResponse> signup(@Valid @RequestBody MemberCreateRequest request) {
        MemberCreateResponse response = memberService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/members/me")
    public ResponseEntity<MemberMeResponse> getMe(@AuthenticationPrincipal Jwt jwt) {

        Long memberId = Long.valueOf(jwt.getSubject());

        MemberMeResponse response = memberService.getMe(memberId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    
}
