package io.github.qlrxn1152.footballv3.team.exception.exceptions;

public class CanNotDisbandTeamException extends RuntimeException {
    public CanNotDisbandTeamException() {
        super("팀을 해체하기 위해서는, 팀원이 본인 1명 뿐이어야 합니다.");
    }
}
