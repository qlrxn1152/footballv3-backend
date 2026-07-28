package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class AlreadyExistTeamMatchResultException extends RuntimeException {
    public AlreadyExistTeamMatchResultException() {
        super("이미 결과가 등록된 매치입니다.");
    }
}
