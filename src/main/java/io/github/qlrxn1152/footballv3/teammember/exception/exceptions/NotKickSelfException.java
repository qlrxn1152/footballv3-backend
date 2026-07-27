package io.github.qlrxn1152.footballv3.teammember.exception.exceptions;

public class NotKickSelfException extends RuntimeException {
    public NotKickSelfException() {
        super("자기 자신을 강퇴할 수 없습니다.");
    }
}
