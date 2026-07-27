package io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions;

public class NotSameTeamException extends RuntimeException {
    public NotSameTeamException() {
        super("해당팀이 아닙니다.");
    }
}
