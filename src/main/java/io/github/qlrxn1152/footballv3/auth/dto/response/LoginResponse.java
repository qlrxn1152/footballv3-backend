package io.github.qlrxn1152.footballv3.auth.dto.response;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import io.github.qlrxn1152.footballv3.member.domain.MemberRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {

    private Long memberId;
    private String username;
    private MemberRole role;

    public static LoginResponse of(Member member) {
        return new LoginResponse(member.getId(), member.getUsername(), member.getRole());
    }
}
