package io.github.qlrxn1152.footballv3.teammatch.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamMatchResultCreateRequest {

    @NotNull(message = "홈팀 점수는 필수입니다.")
    @Min(value = 0, message = "점수는 0점 이상이여야 합니다.")
    private Integer homeScore; // int -> 0, null 구분이안됨

    @NotNull(message = "어웨이팀 점수는 필수입니다.")
    @Min(value = 0, message = "점수는 0점 이상이여야 합니다.")
    private Integer awayScore;
}
