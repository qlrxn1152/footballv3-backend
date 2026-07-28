package io.github.qlrxn1152.footballv3.teammatch.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamMatchCreateRequest {

    @NotNull(message = "경기 예정 일시는 필수입니다.")
    @Future(message = "경기 진행 일시는 현재 시간 이후여야 합니다.")
    private LocalDateTime playedAt;

}
