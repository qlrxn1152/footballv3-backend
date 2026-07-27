package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class SameTeamNameException extends RuntimeException {
    public SameTeamNameException() {
        super("현재 팀 이름과 동일합니다.");
    }
}
