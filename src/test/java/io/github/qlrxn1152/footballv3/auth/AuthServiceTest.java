package io.github.qlrxn1152.footballv3.auth;

import io.github.qlrxn1152.footballv3.auth.dto.request.MemberLoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.MemberLoginResponse;
import io.github.qlrxn1152.footballv3.auth.exception.exceptions.InvalidLoginPasswordException;
import io.github.qlrxn1152.footballv3.auth.exception.exceptions.InvalidLoginUsernameException;
import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.domain.MemberRole;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrateTest
public class AuthServiceTest {

    @Autowired private MemberService memberService;
    @Autowired private AuthService authService;

    @Autowired private MemberRepository memberRepository;

    @Autowired private JwtDecoder jwtDecoder;
    @Autowired private MemberFixture memberFixture;


    private MemberLoginResponse loginMember(String username, String password) {
        return authService.login(MemberLoginRequest.of(username, password));
    }


    @Nested
    class MemberLogin {

        @Test
        @DisplayName(value = "로그인 성공")
        void login() throws Exception {
            // given
            memberFixture.signupMember("userA", "1234");
            Member member = memberRepository.findByUsername("userA").get();

            // when
            MemberLoginResponse response = loginMember("userA", "1234");
            Jwt jwt = jwtDecoder.decode(response.getAccessToken());

            // then
            assertThat(response.getMemberId()).isEqualTo(member.getId());
            assertThat(response.getRole()).isEqualTo(MemberRole.USER);

            assertThat(Long.valueOf(jwt.getSubject())).isEqualTo(member.getId());
        }

        @Test
        @DisplayName(value = "로그인 실패_존재하지 않는 아이디")
        void login_fail_invalid_username() throws Exception {
            // given
            memberFixture.signupMember("userA", "1234");

            // when && then
            assertThatThrownBy(() -> loginMember("userB", "1234"))
                    .isInstanceOf(InvalidLoginUsernameException.class)
                    .hasMessage("아이디가 올바르지 않습니다.");

        }

        @Test
        @DisplayName(value = "로그인 실패_존재하지 않는 비밀번호")
        void login_fail_invalid_password() throws Exception {
            // given
            memberFixture.signupMember("userA", "1234");

            // when && then
            assertThatThrownBy(() -> loginMember("userA", "1q2w3e"))
                    .isInstanceOf(InvalidLoginPasswordException.class)
                    .hasMessage("비밀번호가 올바르지 않습니다.");
        }
    }
}
