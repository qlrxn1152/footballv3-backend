package io.github.qlrxn1152.footballv3.auth.exception.exceptions;

public class InvalidLoginPasswordException extends RuntimeException {
    public InvalidLoginPasswordException() {
        super("비밀번호가 올바르지 않습니다.");
    }
}
