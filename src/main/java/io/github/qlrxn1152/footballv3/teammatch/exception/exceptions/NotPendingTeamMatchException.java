package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class NotPendingTeamMatchException extends RuntimeException {
    public NotPendingTeamMatchException() {
        super("대기 중인 매치가 아닙니다.");
    }
}
