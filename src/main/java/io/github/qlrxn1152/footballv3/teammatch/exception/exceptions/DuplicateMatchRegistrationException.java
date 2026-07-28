package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class DuplicateMatchRegistrationException extends RuntimeException {
    public DuplicateMatchRegistrationException() {
        super("이미 대기중인 매치가 존재합니다.");
    }
}
