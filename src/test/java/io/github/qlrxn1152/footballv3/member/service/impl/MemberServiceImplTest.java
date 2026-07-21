package io.github.qlrxn1152.footballv3.member.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.DuplicateUsernameException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceImplTest {

    @Autowired private MemberService memberService;
    @Autowired private MemberRepository memberRepository;

    @Autowired private PasswordEncoder encoder;

    @Test
    @DisplayName(value = "회원가입 성공")
    void signup() throws Exception {
        // given
        MemberCreateRequest request = new MemberCreateRequest("test", "1234");

        // when
        MemberCreateResponse response = memberService.signup(request);
        Member memberEntity = memberRepository.findById(response.getMemberId()).get();

        // then
        assertThat(response).isNotNull();
        assertThat(memberEntity).isNotNull();

        assertThat(response.getUsername()).isEqualTo("test");
        assertThat(encoder.matches("1234", memberEntity.getPassword())).isTrue();
        assertThat(memberEntity.getUsername()).isEqualTo(response.getUsername());
    }

    @Test
    @DisplayName(value = "회원가입 성공 ( 공백제거, 대문자를 소문자 ) ")
    void signup_2() throws Exception {
        // given
        MemberCreateRequest request = new MemberCreateRequest("   tEst      ", "1234");

        // when
        MemberCreateResponse response = memberService.signup(request);
        Member memberEntity = memberRepository.findById(response.getMemberId()).get();

        // then
        assertThat(response).isNotNull();
        assertThat(memberEntity).isNotNull();

        assertThat(response.getUsername()).isEqualTo("test");
        assertThat(encoder.matches("1234", memberEntity.getPassword())).isTrue();
        assertThat(memberEntity.getUsername()).isEqualTo(response.getUsername());
    }



    @Test
    @DisplayName(value = "회원가입_실패_중복 이름")
    void signup_duplicateUsername() throws Exception {
        // given
        MemberCreateRequest request = new MemberCreateRequest("test", "1234");
        memberService.signup(request);

        // when && then
        assertThatThrownBy(() -> memberService.signup(new MemberCreateRequest("test", "1241244414")))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage("아이디 중복");
    }


}