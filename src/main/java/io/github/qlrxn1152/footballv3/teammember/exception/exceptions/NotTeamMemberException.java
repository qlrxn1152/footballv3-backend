package io.github.qlrxn1152.footballv3.teammember.exception.exceptions;

public class NotTeamMemberException extends RuntimeException {
    public NotTeamMemberException() {
        super("해당팀의 일반 유저가 아닙니다.");
    }
}
