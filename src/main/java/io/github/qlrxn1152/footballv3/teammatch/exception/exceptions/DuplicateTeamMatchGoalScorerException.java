package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class DuplicateTeamMatchGoalScorerException extends RuntimeException {
    public DuplicateTeamMatchGoalScorerException() {
        super("이미 해당매치에 등록된 득점자입니다.");
    }
}
