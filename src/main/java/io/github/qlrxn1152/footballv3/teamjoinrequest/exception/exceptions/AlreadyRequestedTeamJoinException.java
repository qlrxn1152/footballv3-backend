package io.github.qlrxn1152.footballv3.teamjoinrequest.exception.exceptions;

public class AlreadyRequestedTeamJoinException extends RuntimeException {
    public AlreadyRequestedTeamJoinException() {
        super("이미 팀 가입신청이 존재합니다.");
    }
}
