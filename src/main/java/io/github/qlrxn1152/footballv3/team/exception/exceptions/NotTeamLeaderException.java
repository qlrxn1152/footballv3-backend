package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class NotTeamLeaderException extends RuntimeException {
    public NotTeamLeaderException() {
        super("팀장이 아닙니다.");
    }
}
