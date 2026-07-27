package io.github.qlrxn1152.footballv3.team.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamLeaderTransferRequest {

    @NotNull(message = "새로운 팀장 회원 ID는 필수입니다.")
    private Long newLeaderMemberId;
}
