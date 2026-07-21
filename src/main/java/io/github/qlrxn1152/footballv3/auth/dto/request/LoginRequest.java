package io.github.qlrxn1152.footballv3.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "아이디를 입력하세요.")
    @Size(min = 4, max = 12, message = "아이디 또는 비밀번호를 확인하세요.")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 4, max = 15, message = "아이디 또는 비밀번호를 확인하세요.")
    private String password;

    public static LoginRequest of(String username, String password) {
        return new LoginRequest(username, password);
    }

}
