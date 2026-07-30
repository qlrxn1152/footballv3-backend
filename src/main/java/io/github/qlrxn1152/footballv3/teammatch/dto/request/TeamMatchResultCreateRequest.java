package io.github.qlrxn1152.footballv3.teammatch.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @NotNull @Valid
    private List<Scorer> homeScorers;

    @NotNull @Valid
    private List<Scorer> awayScorers;

    @Getter
    @NoArgsConstructor(access =  AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Scorer {

        @NotNull(message = "득점자 ID 는 필수입니다.")
        private Long memberId;

        @NotNull(message = "득점 수는 필수입니다.")
        @Min(value = 1, message = "득점 수는 1 이상이어야 합니다.")
        private Integer goalCount;
    }

}
