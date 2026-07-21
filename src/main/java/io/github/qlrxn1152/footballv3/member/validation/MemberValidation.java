package io.github.qlrxn1152.footballv3.member.validation;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.DuplicateUsernameException;
import io.github.qlrxn1152.footballv3.member.exception.exceptions.NotFoundMemberException;
import io.github.qlrxn1152.footballv3.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberValidation {

    private final MemberRepository memberRepository;

    public void validateDuplicateUsername(String username) {
        if ( memberRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException();
        }
    }

    public Member validateExistMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
    }


}
