package io.github.qlrxn1152.footballv3.member.service.impl;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberMeResponse;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import io.github.qlrxn1152.footballv3.member.validation.MemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final MemberValidator memberValidator;


    @Override
    public MemberCreateResponse signup(MemberCreateRequest request) {
        String normalizedUsername = request.getUsername().toLowerCase().strip();

        memberValidator.validateDuplicateUsername(normalizedUsername);
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member savedMember = memberRepository.save(Member.signup(normalizedUsername, encodedPassword));
        return MemberCreateResponse.of(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberMeResponse getMe(Long memberId) {
        Member member = memberValidator.validateExistMemberAndReturn(memberId);
        return MemberMeResponse.of(member);
    }
}
