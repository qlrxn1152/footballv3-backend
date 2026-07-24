package io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions;

public class NotSameTeamException extends RuntimeException {
    public NotSameTeamException() {
        super("해당 팀의 가입신청이 아닙니다.");
    }
}
