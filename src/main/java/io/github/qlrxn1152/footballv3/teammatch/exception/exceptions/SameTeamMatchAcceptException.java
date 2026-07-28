package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class SameTeamMatchAcceptException extends RuntimeException {
    public SameTeamMatchAcceptException() {
        super("자신의 팀이 등록한 매치는 수락할 수 없습니다.");
    }
}
