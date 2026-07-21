package io.github.qlrxn1152.footballv3.member.service;

import io.github.qlrxn1152.footballv3.member.dto.request.MemberCreateRequest;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberCreateResponse;
import io.github.qlrxn1152.footballv3.member.dto.response.MemberMeResponse;

public interface MemberService {

    MemberCreateResponse signup(MemberCreateRequest request);

    MemberMeResponse getMe(Long memberId);
}
