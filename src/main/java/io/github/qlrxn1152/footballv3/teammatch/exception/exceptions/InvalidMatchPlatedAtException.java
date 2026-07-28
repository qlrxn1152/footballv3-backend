package io.github.qlrxn1152.footballv3.teammatch.exception.exceptions;

public class InvalidMatchPlatedAtException extends RuntimeException {
    public InvalidMatchPlatedAtException() {
        super("경기 예정 일시는 현재 시간보다 미래여야 합니다.");
    }
}
