package io.github.qlrxn1152.footballv3.member.exception.exceptions;

public class NotFoundMemberException extends RuntimeException {
    public NotFoundMemberException() {
        super("멤버 조회 실패");
    }
}
