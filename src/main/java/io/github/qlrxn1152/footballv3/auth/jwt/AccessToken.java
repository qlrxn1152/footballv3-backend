package io.github.qlrxn1152.footballv3.auth.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessToken {

    private String value;
    private long expiresIn;

    public static AccessToken of(
            String value,
            long expiresIn
    ) {
        return new AccessToken(value, expiresIn);
    }


}