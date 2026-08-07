package io.github.qlrxn1152.footballv3.member.exception.exceptions;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException() {
        super("공백은 허용하지 않습니다.");
    }
}
