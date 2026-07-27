package io.github.qlrxn1152.footballv3.teammember.exception.exceptions;

public class NotJoinedTeamException extends RuntimeException {
    public NotJoinedTeamException() {
        super("팀에 속한 회원이 아닙니다.");
    }
}
