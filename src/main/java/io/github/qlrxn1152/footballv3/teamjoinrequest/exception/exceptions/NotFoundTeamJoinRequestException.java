package io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions;

public class NotFoundTeamJoinRequestException extends RuntimeException {
    public NotFoundTeamJoinRequestException() {
        super("가입신청 조회 실패");
    }
}
