package io.github.qlrxn1152.footballv3.member.exception.exceptions;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super("아이디 중복");
    }
}
