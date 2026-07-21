package io.github.qlrxn1152.footballv3.auth.exception.exceptions;

public class LoginException extends RuntimeException {
    public LoginException() {
        super("아이디 또는 비밀번호가 올바르지 않습니다.");

    }
}
