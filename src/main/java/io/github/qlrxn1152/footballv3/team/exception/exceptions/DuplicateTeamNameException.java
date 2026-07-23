package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class DuplicateTeamNameException extends RuntimeException {
    public DuplicateTeamNameException() {
        super("팀 이름 중복");
    }
}
