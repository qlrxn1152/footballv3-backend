package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class InvalidTeamMatchScoreException extends RuntimeException {
    public InvalidTeamMatchScoreException() {
        super("경기 점수는 0 이상이어야 합니다.");
    }
}
