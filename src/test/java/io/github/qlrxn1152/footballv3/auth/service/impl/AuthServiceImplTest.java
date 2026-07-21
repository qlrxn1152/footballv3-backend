package io.github.qlrxn1152.footballv3.auth.service.impl;

import io.github.qlrxn1152.footballv3.auth.dto.request.LoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.LoginResponse;
import io.github.qlrxn1152.footballv3.auth.exception.exceptions.LoginException;
import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
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
class AuthServiceImplTest {

    @Autowired private AuthService authService;
    @Autowired private MemberService memberService;

    @Test
    @DisplayName("로그인 성공")
    void login() {
        // given
        MemberCreateResponse signupResponse = memberService.signup(new MemberCreateRequest("test", "1234"));

        // when
        LoginResponse response = authService.login(new LoginRequest("test", "1234"));

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(signupResponse.getMemberId());
        assertThat(response.getUsername()).isEqualTo("test");
    }

    @Test
    @DisplayName("로그인 성공_아이디에 공백")
    void login_strip() {
        // given
        MemberCreateResponse signupResponse = memberService.signup(new MemberCreateRequest("test", "1234"));

        // when
        LoginResponse response = authService.login(new LoginRequest("test", "1234"));

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(signupResponse.getMemberId());
        assertThat(response.getUsername()).isEqualTo("test");
    }

    @Test
    @DisplayName("로그인 실패_아이디 불일치")
    void login_fail_username() {
        // given
        memberService.signup(new MemberCreateRequest("test", "1234"));

        // when
        assertThatThrownBy(() -> authService.login(new LoginRequest("testttt", "1234")))
                .isInstanceOf(LoginException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패_비밀번호 불일치")
    void login_fail_password() {
        // given
        memberService.signup(new MemberCreateRequest("test", "1234"));

        // when
        assertThatThrownBy(() -> authService.login(new LoginRequest("test", "123444444")))
                .isInstanceOf(LoginException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
    }





}