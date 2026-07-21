package io.github.qlrxn1152.footballv3.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberCreateRequest {

    @NotBlank
    @Size(min = 4, max = 12)
    private String username;

    @Size(min = 4, max = 15)
    private String password;


}
