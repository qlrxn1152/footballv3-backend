package io.github.qlrxn1152.footballv3.teammember.exception.exceptions;

public class AlreadyJoinedTeamException extends RuntimeException {
    public AlreadyJoinedTeamException() {
        super("이미 팀에 속한 회원입니다.");
    }
}
