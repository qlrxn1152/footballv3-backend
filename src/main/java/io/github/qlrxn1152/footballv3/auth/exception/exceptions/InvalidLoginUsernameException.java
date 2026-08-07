package io.github.qlrxn1152.footballv3.auth.exception.exceptions;

public class InvalidLoginUsernameException extends RuntimeException {
    public InvalidLoginUsernameException() {
        super("아이디가 올바르지 않습니다.");

    }
}
