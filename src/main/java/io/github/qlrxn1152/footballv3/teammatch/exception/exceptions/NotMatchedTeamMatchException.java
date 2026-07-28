package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class NotMatchedTeamMatchException extends RuntimeException {
    public NotMatchedTeamMatchException() {
        super("진행 중인 매치가 아닙니다.");
    }
}
