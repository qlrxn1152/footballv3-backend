package io.github.qlrxn1152.footballv3.auth.service.impl;

import io.github.qlrxn1152.footballv3.auth.dto.request.LoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.LoginResponse;
import io.github.qlrxn1152.footballv3.auth.exception.exceptions.LoginException;
import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedUsername = request.getUsername().toLowerCase().strip();

        Member member = memberRepository.findByUsername(normalizedUsername)
                .orElseThrow(LoginException::new);

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new LoginException();
        }

        // 로그인 시킴 -> 서버가, 유저에게 JWT 토큰을 발급한다.
        return LoginResponse.of(member);
    }

}
