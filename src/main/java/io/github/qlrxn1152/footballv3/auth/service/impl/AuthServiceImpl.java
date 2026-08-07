package io.github.qlrxn1152.footballv3.auth.service.impl;

import io.github.qlrxn1152.footballv3.auth.dto.request.MemberLoginRequest;
import io.github.qlrxn1152.footballv3.auth.dto.response.MemberLoginResponse;
import io.github.qlrxn1152.footballv3.auth.exception.exceptions.InvalidLoginUsernameException;
import io.github.qlrxn1152.footballv3.auth.exception.exceptions.InvalidLoginPasswordException;
import io.github.qlrxn1152.footballv3.auth.jwt.AccessToken;
import io.github.qlrxn1152.footballv3.auth.jwt.JwtTokenProvider;
import io.github.qlrxn1152.footballv3.auth.service.AuthService;
import io.github.qlrxn1152.footballv3.member.domain.Member;
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

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public MemberLoginResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
                .orElseThrow(InvalidLoginUsernameException::new);

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidLoginPasswordException();
        }

        // 로그인 시킴 -> 서버가, 유저에게 JWT 토큰을 발급한다.
        AccessToken accessToken = jwtTokenProvider.createAccessToken(member);

        return MemberLoginResponse.of(member, accessToken);
    }

}
