package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class TeamNameLengthException extends RuntimeException {
    public TeamNameLengthException() {
        super("팀 이름은 2~10글자까지만 가능합니다.");
    }
}
