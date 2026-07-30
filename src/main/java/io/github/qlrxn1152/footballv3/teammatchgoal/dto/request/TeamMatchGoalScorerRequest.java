package io.github.qlrxn1152.footballv3.teammatchgoal.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamMatchGoalScorerRequest {

    @NotNull
    private Long memberId;

    @NotNull
    @Min(value = 1, message = "득점 수는 1 이상이어야 합니다.")
    private Integer goalCount;

}
