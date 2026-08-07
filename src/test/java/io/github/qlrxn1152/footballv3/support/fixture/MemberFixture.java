package io.github.qlrxn1152.footballv3.support.fixture;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFixture {

    private final MemberService memberService;

    public MemberCreateResponse signupMember(String username, String password) {
        return memberService.signup(new MemberCreateRequest(username, password));
    }

}
