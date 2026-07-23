package io.github.qlrxn1152.footballv3.team.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TeamCreateRequest {

    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "팀 이름은 2~10 글자까지만 가능합니다.")
    private String teamName;

}
