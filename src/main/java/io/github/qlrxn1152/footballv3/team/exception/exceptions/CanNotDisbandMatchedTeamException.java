package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class CanNotDisbandMatchedTeamException extends RuntimeException {
    public CanNotDisbandMatchedTeamException() {
        super("성사된 매치가 존재하는 팀은 해체할 수 없습니다.");
    }
}
