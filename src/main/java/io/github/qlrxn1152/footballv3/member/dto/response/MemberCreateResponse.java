package io.github.qlrxn1152.footballv3.member.dto.response;

import io.github.qlrxn1152.footballv3.member.domain.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberCreateResponse {

    private Long memberId;
    private String username;
    private LocalDateTime createdAt;

    public static MemberCreateResponse of(Member member) {
        return new MemberCreateResponse(member.getId(), member.getUsername(), member.getCreatedAt());
    }


}
