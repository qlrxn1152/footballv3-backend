package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class NotFoundTeamMatchException extends RuntimeException {
    public NotFoundTeamMatchException() {
        super("팀 매치 조회 실패");
    }
}
