package io.github.qlrxn1152.footballv3.member;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.DuplicateUsernameException;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.InvalidUsernameException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.support.IntegrateTest;
import io.github.qlrxn1152.footballv3.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrateTest
class MemberServiceTest {

    @Autowired private MemberService memberService;
    @Autowired private MemberFixture memberFixture;

    @Autowired private MemberRepository memberRepository;

    @Autowired private PasswordEncoder encoder;



    @Nested
    class MemberSignup {

        @Test
        @DisplayName(value = "회원가입 성공")
        void signup() throws Exception {
            // given
            String username = "userA";
            String password = "1234";

            // when
            MemberCreateResponse response = memberFixture.signupMember(username, password);
            Member memberEntity = memberRepository.findById(response.getMemberId()).get();

            // then
            assertThat(response.getUsername()).isEqualTo(username);
            assertThat(memberEntity.getUsername()).isEqualTo(username);
            assertThat(encoder.matches(password, memberEntity.getPassword())).isTrue();
        }




        @Test
        @DisplayName(value = "회원가입 실패_공백")
        void signup_fail_strip() throws Exception {
            // given
            String username = "     userA  ";
            String password = "1234";

            // when
            assertThatThrownBy(() -> memberFixture.signupMember(username, password))
                    .isInstanceOf(InvalidUsernameException.class)
                    .hasMessage("공백은 허용하지 않습니다.");
        }



        @Test
        @DisplayName(value = "회원가입_실패_중복 이름")
        void signup_fail_duplicateUsername() throws Exception {
            // given
            String username = "userA";
            String password = "1234";

            memberFixture.signupMember(username, password);

            // when && then
            assertThatThrownBy(() -> memberFixture.signupMember("userA", "q1w2e3"))
                    .isInstanceOf(DuplicateUsernameException.class)
                    .hasMessage("아이디 중복");
        }
    }

}