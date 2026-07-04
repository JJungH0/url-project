package com.url.jjung.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public record RegisterReq(
        @Email(message = "이메일 형식이 올바르지 않습니다. 예시 (= xxxx@xxxx.xxx")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[@$!%*#?&]).{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 포함하여 8자 이상 20자 이하로 입력해주세요."
        )
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname) {
}

