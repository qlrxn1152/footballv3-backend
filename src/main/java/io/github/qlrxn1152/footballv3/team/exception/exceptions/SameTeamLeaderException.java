package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class SameTeamLeaderException extends RuntimeException {
    public SameTeamLeaderException() {
        super("자기 자신에게 팀장 위임은 불가합니다.");
    }
}
