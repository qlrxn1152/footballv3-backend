package io.github.qlrxn1152.footballv3.auth.controller;

import io.github.qlrxn1152.footballv3.auth.dto.request.MemberLoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.MemberLoginResponse;
import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<MemberLoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        MemberLoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

}
