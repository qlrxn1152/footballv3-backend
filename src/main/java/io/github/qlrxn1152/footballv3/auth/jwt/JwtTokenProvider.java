package io.github.qlrxn1152.footballv3.auth.jwt;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AccessToken createAccessToken(Member member) {
        Instant issuedAt = Instant.now();

        long expiresIn =
                jwtProperties.getAccessTokenExpirationSeconds();

        Instant expiresAt =
                issuedAt.plusSeconds(expiresIn);

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(member.getId().toString()) // => 어떤 멤버인지
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", member.getUsername())
                .claim("role", member.getRole().name())
                .build();

        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        );

        return AccessToken.of(
                jwt.getTokenValue(),
                expiresIn
        );
    }
}