package io.github.qlrxn1152.footballv3.auth.dto.response;

import io.github.qlrxn1152.footballv3.auth.jwt.AccessToken;
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

    private static final String TOKEN_TYPE = "Bearer";

    private Long memberId;
    private String username;
    private MemberRole role;

    private String accessToken;
    private String tokenType;
    private long expiresIn;

    public static LoginResponse of(Member member, AccessToken accessToken) {
        return new LoginResponse(
                member.getId(),
                member.getUsername(),
                member.getRole(),
                accessToken.getValue(),
                TOKEN_TYPE,
                accessToken.getExpiresIn()
        );
    }

}
